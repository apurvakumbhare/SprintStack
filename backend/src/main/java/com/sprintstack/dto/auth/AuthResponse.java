package com.sprintstack.dto.auth;

public record AuthResponse(String token, String tokenType, UserResponse user) { }
