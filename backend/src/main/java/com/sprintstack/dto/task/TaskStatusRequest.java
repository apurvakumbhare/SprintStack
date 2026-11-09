package com.sprintstack.dto.task;

import com.sprintstack.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(@NotNull TaskStatus status) { }
