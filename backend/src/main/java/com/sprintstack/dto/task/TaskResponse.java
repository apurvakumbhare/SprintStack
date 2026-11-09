package com.sprintstack.dto.task;

import com.sprintstack.entity.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(Long id, String title, String description, Long projectId, String projectName,
                           Long assigneeId, String assigneeName, Long createdById, String createdByName,
                           String priority, String status, LocalDate startDate, LocalDate dueDate,
                           Integer estimatedEffort, LocalDateTime completedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                           List<String> labels) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getProject().getId(),
                task.getProject().getName(), task.getAssignee() == null ? null : task.getAssignee().getId(),
                task.getAssignee() == null ? null : task.getAssignee().getFullName(), task.getCreatedBy().getId(),
                task.getCreatedBy().getFullName(), task.getPriority().name(), task.getStatus().name(), task.getStartDate(),
                task.getDueDate(), task.getEstimatedEffort(), task.getCompletedAt(), task.getCreatedAt(), task.getUpdatedAt(),
                task.getLabels().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList());
    }
}
