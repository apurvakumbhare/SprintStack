package com.sprintstack.service;

import com.sprintstack.dto.task.TaskRequest;
import com.sprintstack.dto.task.TaskResponse;
import com.sprintstack.entity.*;
import com.sprintstack.repository.ProjectMemberRepository;
import com.sprintstack.repository.TaskRepository;
import com.sprintstack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final ProjectService projects;
    private final ProjectMemberRepository members;
    private final UserRepository users;
    private final WorkspaceEventService events;

    public TaskService(TaskRepository tasks, ProjectService projects, ProjectMemberRepository members, UserRepository users, WorkspaceEventService events) {
        this.tasks=tasks; this.projects=projects; this.members=members; this.users=users; this.events=events;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> list(String email, Long projectId, TaskStatus status, TaskPriority priority,
                                   Long assigneeId, boolean mine, boolean overdue, String query) {
        Project project = projects.projectForMember(email, projectId);
        Long effectiveAssignee = mine ? projects.userByEmail(email).getId() : assigneeId;
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return tasks.searchProjectTasks(project.getId(), status, priority, effectiveAssignee, overdue, LocalDate.now(), TaskStatus.COMPLETED, normalizedQuery)
                .stream().map(TaskResponse::from).toList();
    }

    @Transactional
    public TaskResponse create(String email, Long projectId, TaskRequest request) {
        validateDates(request.startDate(), request.dueDate());
        User actor = projects.userByEmail(email);
        Project project = projects.projectForMember(email, projectId);
        User assignee = assignee(projectId, request.assigneeId());
        Task task = new Task(request.title().trim(), clean(request.description()), project, assignee, actor,
                request.priority() == null ? TaskPriority.MEDIUM : request.priority(),
                request.status() == null ? TaskStatus.TODO : request.status(), request.startDate(), request.dueDate(), request.estimatedEffort());
        task.setLabels(labels(request.labels()));
        task = tasks.save(task);
        events.record(actor,project,"TASK_CREATED","TASK",task.getId(),"Created task “"+task.getTitle()+"”");
        if(assignee!=null&&!assignee.getId().equals(actor.getId()))events.notify(assignee,"TASK_ASSIGNED","You were assigned task “"+task.getTitle()+"”","TASK",task.getId());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse update(String email, Long taskId, TaskRequest request) {
        validateDates(request.startDate(), request.dueDate());
        User actor = projects.userByEmail(email);
        Task task = taskById(taskId);
        requireMember(task.getProject().getId(), actor.getId());
        if (!task.getCreatedBy().getId().equals(actor.getId()) && !isManager(task.getProject().getId(), actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the task creator or project manager may edit this task");
        }
        User previousAssignee=task.getAssignee();
        TaskStatus previousStatus=task.getStatus();
        User nextAssignee=assignee(task.getProject().getId(), request.assigneeId());
        TaskStatus nextStatus=request.status() == null ? previousStatus : request.status();
        task.update(request.title().trim(), clean(request.description()), nextAssignee,
                request.priority() == null ? task.getPriority() : request.priority(),
                nextStatus, request.startDate(), request.dueDate(), request.estimatedEffort(), labels(request.labels()));
        events.record(actor,task.getProject(),"TASK_UPDATED","TASK",task.getId(),"Updated task “"+task.getTitle()+"”");
        if(previousStatus!=nextStatus)events.record(actor,task.getProject(),"TASK_STATUS_CHANGED","TASK",task.getId(),"Moved “"+task.getTitle()+"” to "+nextStatus.name().replace('_',' ').toLowerCase());
        if(previousStatus!=nextStatus&&nextStatus==TaskStatus.COMPLETED&&!task.getCreatedBy().getId().equals(actor.getId()))events.notify(task.getCreatedBy(),"TASK_COMPLETED","Task “"+task.getTitle()+"” was completed","TASK",task.getId());
        if(nextAssignee!=null&&(previousAssignee==null||!previousAssignee.getId().equals(nextAssignee.getId()))&&!nextAssignee.getId().equals(actor.getId()))events.notify(nextAssignee,"TASK_ASSIGNED","You were assigned task “"+task.getTitle()+"”","TASK",task.getId());
        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse changeStatus(String email, Long taskId, TaskStatus status) {
        User actor = projects.userByEmail(email);
        Task task = taskById(taskId);
        Long projectId = task.getProject().getId();
        requireMember(projectId, actor.getId());
        boolean assigned = task.getAssignee() != null && task.getAssignee().getId().equals(actor.getId());
        if (!assigned && !task.getCreatedBy().getId().equals(actor.getId()) && !isManager(projectId, actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assignee, task creator, or project manager may change this status");
        }
        TaskStatus previous=task.getStatus();task.changeStatus(status);
        if(previous!=status)events.record(actor,task.getProject(),"TASK_STATUS_CHANGED","TASK",task.getId(),"Moved “"+task.getTitle()+"” to "+status.name().replace('_',' ').toLowerCase());
        if(previous!=status&&status==TaskStatus.COMPLETED&&!task.getCreatedBy().getId().equals(actor.getId()))events.notify(task.getCreatedBy(),"TASK_COMPLETED","Task “"+task.getTitle()+"” was completed","TASK",task.getId());
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(String email, Long taskId) {
        User actor = projects.userByEmail(email);
        Task task = taskById(taskId);
        Long projectId = task.getProject().getId();
        requireMember(projectId, actor.getId());
        if (!task.getCreatedBy().getId().equals(actor.getId()) && !isManager(projectId, actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the task creator or project manager may delete this task");
        }
        events.record(actor,task.getProject(),"TASK_DELETED","TASK",task.getId(),"Deleted task “"+task.getTitle()+"”");
        tasks.delete(task);
    }

    private User assignee(Long projectId, Long userId) {
        if (userId == null) return null;
        if (!members.existsByProject_IdAndUser_Id(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The assignee must be a member of this project");
        }
        return users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee is not available"));
    }

    private Task taskById(Long id) {
        return tasks.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private void requireMember(Long projectId, Long userId) { projects.requireMember(projectId, userId); }
    private boolean isManager(Long projectId, Long userId) {
        return members.findByProject_IdAndUser_Id(projectId, userId)
                .map(member -> member.getRole() == ProjectRole.OWNER || member.getRole() == ProjectRole.PROJECT_MANAGER)
                .orElse(false);
    }
    private static void validateDates(LocalDate start, LocalDate due) {
        if (start != null && due != null && due.isBefore(start)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be before the start date");
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static Set<String> labels(List<String> input) {
        if (input == null) return Set.of();
        var unique = new LinkedHashMap<String, String>();
        for (String label : input) {
            String cleaned = label.trim();
            unique.putIfAbsent(cleaned.toLowerCase(java.util.Locale.ROOT), cleaned);
        }
        return Set.copyOf(unique.values());
    }
}
