package com.sprintstack.dto.issue;

import com.sprintstack.entity.Issue;
import java.time.LocalDateTime;

public record IssueResponse(Long id, String title, String description, Long projectId, String projectName,
                            Long reporterId, String reporterName, Long assigneeId, String assigneeName,
                            String severity, String priority, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static IssueResponse from(Issue issue) {
        return new IssueResponse(issue.getId(),issue.getTitle(),issue.getDescription(),issue.getProject().getId(),issue.getProject().getName(),
                issue.getReporter().getId(),issue.getReporter().getFullName(),issue.getAssignee()==null?null:issue.getAssignee().getId(),
                issue.getAssignee()==null?null:issue.getAssignee().getFullName(),issue.getSeverity().name(),issue.getPriority().name(),
                issue.getStatus().name(),issue.getCreatedAt(),issue.getUpdatedAt());
    }
}
