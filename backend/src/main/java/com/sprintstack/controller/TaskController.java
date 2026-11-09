package com.sprintstack.controller;

import com.sprintstack.dto.task.TaskRequest;
import com.sprintstack.dto.task.TaskResponse;
import com.sprintstack.dto.task.TaskStatusRequest;
import com.sprintstack.dto.task.TaskBulkStatusRequest;
import com.sprintstack.entity.TaskStatus;
import com.sprintstack.entity.TaskPriority;
import com.sprintstack.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class TaskController {
    private final TaskService tasks;
    public TaskController(TaskService tasks) { this.tasks=tasks; }

    @GetMapping("/api/projects/{projectId}/tasks")
    public List<TaskResponse> list(Authentication authentication, @PathVariable Long projectId,
                                   @RequestParam(required = false) TaskStatus status,
                                   @RequestParam(required = false) TaskPriority priority,
                                   @RequestParam(required = false) Long assigneeId,
                                   @RequestParam(defaultValue = "false") boolean mine,
                                   @RequestParam(defaultValue = "false") boolean overdue,
                                   @RequestParam(required = false) String q) {
        return tasks.list(authentication.getName(), projectId, status, priority, assigneeId, mine, overdue, q);
    }

    @PostMapping("/api/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(Authentication authentication, @PathVariable Long projectId,
                               @Valid @RequestBody TaskRequest request) {
        return tasks.create(authentication.getName(), projectId, request);
    }

    @PatchMapping("/api/projects/{projectId}/tasks/status")
    public List<TaskResponse> changeStatuses(Authentication authentication, @PathVariable Long projectId,
                                              @Valid @RequestBody TaskBulkStatusRequest request) {
        return tasks.changeStatuses(authentication.getName(), projectId, request.taskIds(), request.status());
    }

    @PutMapping("/api/tasks/{taskId}")
    public TaskResponse update(Authentication authentication, @PathVariable Long taskId,
                               @Valid @RequestBody TaskRequest request) {
        return tasks.update(authentication.getName(), taskId, request);
    }

    @PatchMapping("/api/tasks/{taskId}/status")
    public TaskResponse changeStatus(Authentication authentication, @PathVariable Long taskId,
                                     @Valid @RequestBody TaskStatusRequest request) {
        return tasks.changeStatus(authentication.getName(), taskId, request.status());
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long taskId) {
        tasks.delete(authentication.getName(), taskId);
    }
}
