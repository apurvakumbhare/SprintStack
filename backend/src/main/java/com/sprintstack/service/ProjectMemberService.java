package com.sprintstack.service;

import com.sprintstack.dto.member.*;
import com.sprintstack.entity.*;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.ProjectRepository;
import com.sprintstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectMemberService {
    private final ProjectService projects;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final WorkspaceEventService events;

    public ProjectMemberService(ProjectService projects,ProjectRepository projectRepository,ProjectMemberRepository members,UserRepository users,WorkspaceEventService events){
        this.projects=projects;this.projectRepository=projectRepository;this.members=members;this.users=users;this.events=events;
    }

    @Transactional(readOnly=true)
    public List<MemberResponse> list(String email,Long projectId){
        User actor=projects.userByEmail(email);projects.requireMember(projectId,actor.getId());
        return members.findAllByProject_IdOrderByJoinedAtAsc(projectId).stream().map(MemberResponse::from).toList();
    }

    @Transactional(readOnly=true)
    public List<UserLookupResponse> searchAvailable(String email,Long projectId,String query){
        User actor=projects.userByEmail(email);projects.requireManager(projectId,actor.getId());
        if(query==null||query.trim().length()<2)return List.of();
        Set<Long> memberIds=members.findAllByProject_IdOrderByJoinedAtAsc(projectId).stream().map(m->m.getUser().getId()).collect(Collectors.toSet());
        String term=query.trim();
        return users.findTop20ByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term,term).stream()
                .filter(user->!memberIds.contains(user.getId())).map(UserLookupResponse::from).toList();
    }

    @Transactional
    public MemberResponse add(String email,Long projectId,MemberRequest request){
        User actor=projects.userByEmail(email);projects.requireManager(projectId,actor.getId());
        ProjectRole role=allowedRole(request.role());
        User user=users.findByEmailIgnoreCase(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No registered user has this email"));
        if(members.existsByProject_IdAndUser_Id(projectId,user.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"This user is already a project member");
        Project project=projectRepository.findById(projectId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project not found"));
        ProjectMember added=members.save(new ProjectMember(project,user,role));
        events.record(actor,project,"MEMBER_ADDED","USER",user.getId(),"Added "+user.getFullName()+" to the project");
        events.notify(user,"PROJECT_MEMBER_ADDED","You were added to project “"+project.getName()+"”","PROJECT",projectId);
        return MemberResponse.from(added);
    }

    @Transactional
    public MemberResponse changeRole(String email,Long projectId,Long memberUserId,MemberRoleRequest request){
        User actor=projects.userByEmail(email);projects.requireManager(projectId,actor.getId());
        ProjectRole role=allowedRole(request.role());
        ProjectMember member=member(projectId,memberUserId);
        if(member.getRole()==ProjectRole.OWNER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"The project owner role cannot be changed here");
        if(member.getRole()==ProjectRole.PROJECT_MANAGER&&role==ProjectRole.DEVELOPER
                &&members.countByProject_IdAndRole(projectId,ProjectRole.PROJECT_MANAGER)+members.countByProject_IdAndRole(projectId,ProjectRole.OWNER)<=1)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Assign another project manager before changing the last manager's role");
        Project project=member.getProject();member.changeRole(role);
        events.record(actor,project,"MEMBER_ROLE_CHANGED","USER",memberUserId,"Changed "+member.getUser().getFullName()+"’s project role to "+role.name());
        return MemberResponse.from(member);
    }

    @Transactional
    public void remove(String email,Long projectId,Long memberUserId){
        User actor=projects.userByEmail(email);projects.requireManager(projectId,actor.getId());
        ProjectMember member=member(projectId,memberUserId);
        if(member.getRole()==ProjectRole.OWNER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Transfer ownership before removing the project owner");
        long managers=members.countByProject_IdAndRole(projectId,ProjectRole.OWNER)+members.countByProject_IdAndRole(projectId,ProjectRole.PROJECT_MANAGER);
        if(managers<=1&&member.getRole()==ProjectRole.PROJECT_MANAGER)throw new ResponseStatusException(HttpStatus.CONFLICT,"Assign another project manager before removing the last manager");
        events.record(actor,member.getProject(),"MEMBER_REMOVED","USER",memberUserId,"Removed "+member.getUser().getFullName()+" from the project");
        events.notify(member.getUser(),"PROJECT_MEMBER_REMOVED","You were removed from project “"+member.getProject().getName()+"”","PROJECT",projectId);
        members.delete(member);
    }

    @Transactional
    public MemberResponse transferOwnership(String email,Long projectId,Long nextOwnerId){
        User actor=projects.userByEmail(email);
        if(actor.getId().equals(nextOwnerId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose another project member to transfer ownership");
        ProjectMember currentOwner=member(projectId,actor.getId());
        if(currentOwner.getRole()!=ProjectRole.OWNER)throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the project owner can transfer ownership");
        ProjectMember nextOwner=member(projectId,nextOwnerId);
        currentOwner.changeRole(ProjectRole.PROJECT_MANAGER);
        nextOwner.changeRole(ProjectRole.OWNER);
        events.record(actor,currentOwner.getProject(),"OWNER_TRANSFERRED","USER",nextOwnerId,"Transferred project ownership to "+nextOwner.getUser().getFullName());
        return MemberResponse.from(nextOwner);
    }

    private ProjectMember member(Long projectId,Long userId){return members.findByProject_IdAndUser_Id(projectId,userId)
            .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Project member not found"));}
    private ProjectRole allowedRole(ProjectRole role){if(role==ProjectRole.OWNER)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Ownership can only be transferred by the project owner");return role;}
}
