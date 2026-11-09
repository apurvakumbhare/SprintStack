package com.sprintstack.dto.project;

import com.sprintstack.entity.Priority;
import com.sprintstack.entity.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProjectUpdateRequest(
        @NotBlank @Size(max = 140) String name,
        @Size(max = 2000) String description,
        @NotNull ProjectStatus status,
        @NotNull Priority priority,
        LocalDate startDate,
        LocalDate dueDate
) { }
