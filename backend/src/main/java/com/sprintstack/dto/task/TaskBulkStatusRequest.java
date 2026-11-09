package com.sprintstack.dto.task;

import com.sprintstack.entity.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TaskBulkStatusRequest(
        @NotEmpty @Size(max = 50) List<@NotNull Long> taskIds,
        @NotNull TaskStatus status) {
}
