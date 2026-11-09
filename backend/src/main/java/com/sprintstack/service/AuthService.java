package com.sprintstack.service;

import com.sprintstack.dto.auth.*;
import com.sprintstack.entity.User;
import com.sprintstack.repository.UserRepository;
import com.sprintstack.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        requireBcryptLength(request.password());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        User user = users.save(new User(request.fullName().trim(), email, passwordEncoder.encode(request.password())));
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return tokenResponse(user);
    }

    @Transactional
    public AuthResponse updateProfile(String email,ProfileUpdateRequest request){
        User user=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account not found"));
        String nextEmail=request.email().trim().toLowerCase(Locale.ROOT);
        if(!user.getEmail().equalsIgnoreCase(nextEmail)&&users.existsByEmailIgnoreCaseAndIdNot(nextEmail,user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"An account with this email already exists");
        user.updateProfile(request.fullName().trim(),nextEmail);
        return tokenResponse(user);
    }

    @Transactional
    public AuthResponse changePassword(String email,PasswordChangeRequest request){
        requireBcryptLength(request.newPassword());
        User user=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Account not found"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(),request.currentPassword()));
        if(passwordEncoder.matches(request.newPassword(),user.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a new password that differs from your current password");
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.from(user);
    }

    private AuthResponse tokenResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer", UserResponse.from(user));
    }

    private void requireBcryptLength(String password){
        if(password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must be 72 bytes or fewer");
    }
}
