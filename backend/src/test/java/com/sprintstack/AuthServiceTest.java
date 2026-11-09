package com.sprintstack;

import com.sprintstack.dto.auth.RegisterRequest;
import com.sprintstack.entity.User;
import com.sprintstack.repository.UserRepository;
import com.sprintstack.security.JwtService;
import com.sprintstack.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private UserRepository users;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(users, new BCryptPasswordEncoder(), mock(AuthenticationManager.class), jwtService);
    }

    @Test
    void registrationHashesPasswordAndReturnsOnlySafeUserFields() {
        when(users.existsByEmailIgnoreCase("person@example.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("signed-token");

        var response = authService.register(new RegisterRequest("  Sample Person ", " Person@Example.com ", "strong-pass-1"));

        assertEquals("signed-token", response.token());
        assertEquals("Sample Person", response.user().fullName());
        assertEquals("person@example.com", response.user().email());
        assertFalse(response.user().toString().contains("strong-pass-1"));
        verify(users).save(argThat(user -> !user.getPasswordHash().equals("strong-pass-1")
                && new BCryptPasswordEncoder().matches("strong-pass-1", user.getPasswordHash())));
    }

    @Test
    void registrationRejectsAnExistingEmail() {
        when(users.existsByEmailIgnoreCase("person@example.com")).thenReturn(true);
        var error = assertThrows(ResponseStatusException.class,
                () -> authService.register(new RegisterRequest("Person", "person@example.com", "strong-pass-1")));
        assertEquals(409, error.getStatusCode().value());
        verify(users, never()).save(any());
    }
}
