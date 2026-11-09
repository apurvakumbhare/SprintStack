package com.sprintstack.dto.issue;

import com.sprintstack.entity.IssueStatus;
import jakarta.validation.constraints.NotNull;

public record IssueStatusRequest(@NotNull IssueStatus status) { }
