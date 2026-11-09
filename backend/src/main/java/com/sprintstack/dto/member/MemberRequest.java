package com.sprintstack.dto.member;

import com.sprintstack.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberRequest(@NotBlank @Email String email, @NotNull ProjectRole role) { }
