package com.sprintstack.dto.project;

import com.sprintstack.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank @Size(max = 140) String name,
        @Size(max = 2000) String description,
        Priority priority,
        LocalDate startDate,
        LocalDate dueDate
) { }
