package com.sprintstack.dto.task;

import com.sprintstack.entity.TaskPriority;
import com.sprintstack.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 4000) String description,
        Long assigneeId,
        TaskPriority priority,
        TaskStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        @PositiveOrZero Integer estimatedEffort
) { }
