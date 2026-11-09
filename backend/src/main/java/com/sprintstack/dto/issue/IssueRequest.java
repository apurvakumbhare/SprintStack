package com.sprintstack.dto.issue;

import com.sprintstack.entity.IssueSeverity;
import com.sprintstack.entity.IssueStatus;
import com.sprintstack.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IssueRequest(@NotBlank @Size(max=180) String title, @Size(max=4000) String description,
                           Long assigneeId, IssueSeverity severity, TaskPriority priority, IssueStatus status) { }
