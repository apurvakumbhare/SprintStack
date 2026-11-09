package com.sprintstack.service;

import com.sprintstack.dto.project.ProjectRequest;
import com.sprintstack.dto.project.ProjectResponse;
import com.sprintstack.dto.project.ProjectUpdateRequest;
import com.sprintstack.entity.*;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.ProjectRepository;
import com.sprintstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final WorkspaceEventService events;

    public ProjectService(ProjectRepository projects, ProjectMemberRepository members, UserRepository users, WorkspaceEventService events) {
        this.projects=projects; this.members=members; this.users=users; this.events=events;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(String email) {
        User user = userByEmail(email);
        return projects.findVisibleToUser(user.getId()).stream().map(ProjectResponse::from).toList();
    }

    @Transactional
    public ProjectResponse create(String email, ProjectRequest request) {
        validateDates(request.startDate(), request.dueDate());
        User user = userByEmail(email);
        Project project = projects.save(new Project(request.name().trim(), clean(request.description()),
                ProjectStatus.PLANNING, request.priority() == null ? Priority.MEDIUM : request.priority(),
                request.startDate(), request.dueDate(), user));
        ProjectMember owner = new ProjectMember(project, user, ProjectRole.OWNER);
        project.getMembers().add(owner);
        members.save(owner);
        events.record(user,project,"PROJECT_CREATED","PROJECT",project.getId(),"Created project “"+project.getName()+"”");
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(String email, Long projectId) {
        Project project = projectById(projectId);
        requireMember(projectId, userByEmail(email).getId());
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(String email, Long projectId, ProjectUpdateRequest request) {
        validateDates(request.startDate(), request.dueDate());
        User user = userByEmail(email);
        requireManager(projectId, user.getId());
        Project project = projectById(projectId);
        project.update(request.name().trim(), clean(request.description()), request.status(), request.priority(),
                request.startDate(), request.dueDate());
        events.record(user,project,"PROJECT_UPDATED","PROJECT",project.getId(),"Updated project “"+project.getName()+"”");
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse archive(String email, Long projectId) {
        User user = userByEmail(email);
        requireManager(projectId, user.getId());
        Project project = projectById(projectId);
        project.archive();
        events.record(user,project,"PROJECT_ARCHIVED","PROJECT",project.getId(),"Archived project “"+project.getName()+"”");
        return ProjectResponse.from(project);
    }

    @Transactional(readOnly = true)
    public Project projectForMember(String email, Long projectId) {
        User user = userByEmail(email);
        requireMember(projectId, user.getId());
        return projectById(projectId);
    }

    public void requireMember(Long projectId, Long userId) {
        if (!members.existsByProject_IdAndUser_Id(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project");
        }
    }

    public void requireManager(Long projectId, Long userId) {
        ProjectMember member = members.findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this project"));
        if (member.getRole() != ProjectRole.OWNER && member.getRole() != ProjectRole.PROJECT_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Project manager access is required");
        }
    }

    public User userByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Project projectById(Long id) {
        return projects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private static void validateDates(java.time.LocalDate start, java.time.LocalDate due) {
        if (start != null && due != null && due.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be before the start date");
        }
    }

    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
