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
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Optional;
import javax.imageio.ImageIO;

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

    @Test
    void profilePhotoIsValidatedNormalizedAndReturnedForTheAuthenticatedAccount() throws Exception {
        User user = new User("Photo Person", "person@example.com", "already-hashed-password");
        when(users.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
        BufferedImage image = new BufferedImage(12, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", bytes);
        String dataUrl = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes.toByteArray());

        var saved = authService.updateProfilePhoto("person@example.com", new com.sprintstack.dto.auth.ProfilePhotoRequest(dataUrl));

        assertEquals(dataUrl.substring(0, dataUrl.indexOf(',') + 1), saved.photo().substring(0, saved.photo().indexOf(',') + 1));
        assertTrue(user.getProfilePhoto().length > 0);
        assertTrue(ImageIO.read(new java.io.ByteArrayInputStream(user.getProfilePhoto())) != null);
        assertEquals(saved, authService.profilePhoto("person@example.com"));
    }

    @Test
    void profilePhotoRejectsNonJpegData() {
        User user = new User("Photo Person", "person@example.com", "already-hashed-password");
        when(users.findByEmailIgnoreCase("person@example.com")).thenReturn(Optional.of(user));
        var error = assertThrows(ResponseStatusException.class,
                () -> authService.updateProfilePhoto("person@example.com", new com.sprintstack.dto.auth.ProfilePhotoRequest("data:image/png;base64,aGVsbG8=")));
        assertEquals(400, error.getStatusCode().value());
    }
}
