package com.sprintstack.dto.member;

import com.sprintstack.entity.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleRequest(@NotNull ProjectRole role) { }
