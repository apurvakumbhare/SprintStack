package com.sprintstack.dto.auth;

import com.sprintstack.entity.User;

public record UserResponse(Long id, String fullName, String email, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getGlobalRole().name());
    }
}
