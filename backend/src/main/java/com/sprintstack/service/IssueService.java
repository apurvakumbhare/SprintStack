package com.sprintstack.service;

import com.sprintstack.dto.issue.IssueRequest;
import com.sprintstack.dto.issue.IssueResponse;
import com.sprintstack.entity.*;
import com.sprintstack.repository.IssueRepository;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class IssueService {
    private final IssueRepository issues;
    private final ProjectService projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final WorkspaceEventService events;

    public IssueService(IssueRepository issues, ProjectService projects, ProjectMemberRepository members, UserRepository users, WorkspaceEventService events) {
        this.issues=issues; this.projects=projects; this.members=members; this.users=users; this.events=events;
    }

    @Transactional(readOnly=true)
    public List<IssueResponse> list(String email, Long projectId, IssueStatus status, IssueSeverity severity, String query) {
        return list(email, projectId, status, severity, null, query);
    }

    @Transactional(readOnly=true)
    public List<IssueResponse> list(String email, Long projectId, IssueStatus status, IssueSeverity severity, TaskPriority priority, String query) {
        Project project=projects.projectForMember(email,projectId);
        return issues.searchProjectIssues(project.getId(),status,severity,priority,query==null||query.isBlank()?null:query.trim())
                .stream().map(IssueResponse::from).toList();
    }

    @Transactional
    public IssueResponse create(String email, Long projectId, IssueRequest request) {
        User reporter=projects.userByEmail(email);
        Project project=projects.projectForMember(email,projectId);
        User assignee=assignee(projectId,request.assigneeId());
        Issue issue=issues.save(new Issue(request.title().trim(),clean(request.description()),project,reporter,assignee,
                request.severity()==null?IssueSeverity.MEDIUM:request.severity(),
                request.priority()==null?TaskPriority.MEDIUM:request.priority()));
        events.record(reporter,project,"ISSUE_REPORTED","ISSUE",issue.getId(),"Reported issue “"+issue.getTitle()+"”");
        if(assignee!=null&&!assignee.getId().equals(reporter.getId()))events.notify(assignee,"ISSUE_ASSIGNED","You were assigned issue “"+issue.getTitle()+"”","ISSUE",issue.getId());
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse update(String email, Long issueId, IssueRequest request) {
        User actor=projects.userByEmail(email);
        Issue issue=issueById(issueId);
        Long projectId=issue.getProject().getId();
        projects.requireMember(projectId,actor.getId());
        if(!issue.getReporter().getId().equals(actor.getId())&&!isManager(projectId,actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the reporter or project manager may edit this issue");
        }
        User previousAssignee=issue.getAssignee();User nextAssignee=assignee(projectId,request.assigneeId());
        IssueStatus previousStatus=issue.getStatus();
        IssueStatus nextStatus=request.status()==null?previousStatus:request.status();
        issue.update(request.title().trim(),clean(request.description()),nextAssignee,
                request.severity()==null?issue.getSeverity():request.severity(),request.priority()==null?issue.getPriority():request.priority(),nextStatus);
        events.record(actor,issue.getProject(),"ISSUE_UPDATED","ISSUE",issue.getId(),"Updated issue “"+issue.getTitle()+"”");
        if(previousStatus!=nextStatus)events.record(actor,issue.getProject(),"ISSUE_STATUS_CHANGED","ISSUE",issue.getId(),"Moved issue “"+issue.getTitle()+"” to "+nextStatus.name().replace('_',' ').toLowerCase());
        if(previousStatus!=nextStatus&&nextStatus==IssueStatus.RESOLVED&&!issue.getReporter().getId().equals(actor.getId()))events.notify(issue.getReporter(),"ISSUE_RESOLVED","Issue “"+issue.getTitle()+"” was resolved","ISSUE",issue.getId());
        if(nextAssignee!=null&&(previousAssignee==null||!previousAssignee.getId().equals(nextAssignee.getId()))&&!nextAssignee.getId().equals(actor.getId()))events.notify(nextAssignee,"ISSUE_ASSIGNED","You were assigned issue “"+issue.getTitle()+"”","ISSUE",issue.getId());
        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse changeStatus(String email, Long issueId, IssueStatus status) {
        User actor=projects.userByEmail(email);
        Issue issue=issueById(issueId);
        Long projectId=issue.getProject().getId();
        projects.requireMember(projectId,actor.getId());
        boolean assigned=issue.getAssignee()!=null&&issue.getAssignee().getId().equals(actor.getId());
        if(!assigned&&!issue.getReporter().getId().equals(actor.getId())&&!isManager(projectId,actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the assignee, reporter, or project manager may update this issue");
        }
        IssueStatus previous=issue.getStatus();issue.update(issue.getTitle(),issue.getDescription(),issue.getAssignee(),issue.getSeverity(),issue.getPriority(),status);
        if(previous!=status)events.record(actor,issue.getProject(),"ISSUE_STATUS_CHANGED","ISSUE",issue.getId(),"Moved issue “"+issue.getTitle()+"” to "+status.name().replace('_',' ').toLowerCase());
        if(previous!=status&&status==IssueStatus.RESOLVED&&!issue.getReporter().getId().equals(actor.getId()))events.notify(issue.getReporter(),"ISSUE_RESOLVED","Issue “"+issue.getTitle()+"” was resolved","ISSUE",issue.getId());
        return IssueResponse.from(issue);
    }

    private User assignee(Long projectId, Long userId) {
        if(userId==null)return null;
        if(!members.existsByProject_IdAndUser_Id(projectId,userId))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"The assignee must be a member of this project");
        return users.findById(userId).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Assignee is not available"));
    }
    private Issue issueById(Long id){return issues.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Issue not found"));}
    private boolean isManager(Long projectId,Long userId){return members.findByProject_IdAndUser_Id(projectId,userId).map(m->m.getRole()==ProjectRole.OWNER||m.getRole()==ProjectRole.PROJECT_MANAGER).orElse(false);}
    private static String clean(String value){return value==null||value.isBlank()?null:value.trim();}
}
