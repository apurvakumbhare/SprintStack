package com.sprintstack.controller;

import com.sprintstack.dto.auth.*;
import com.sprintstack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) { return authService.currentUser(authentication.getName()); }

    @PatchMapping("/profile")
    public AuthResponse updateProfile(Authentication authentication,@Valid @RequestBody ProfileUpdateRequest request){return authService.updateProfile(authentication.getName(),request);}

    @PutMapping("/password")
    public AuthResponse changePassword(Authentication authentication,@Valid @RequestBody PasswordChangeRequest request){return authService.changePassword(authentication.getName(),request);}
}
