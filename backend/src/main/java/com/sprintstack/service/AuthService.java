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
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

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

    @Transactional(readOnly = true)
    public ProfilePhotoResponse profilePhoto(String email) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        byte[] photo = user.getProfilePhoto();
        return new ProfilePhotoResponse(photo == null ? null : "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(photo));
    }

    @Transactional
    public ProfilePhotoResponse updateProfilePhoto(String email, ProfilePhotoRequest request) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.updateProfilePhoto(normalizePhoto(request.photo()));
        return profilePhoto(email);
    }

    private byte[] normalizePhoto(String dataUrl) {
        String prefix = "data:image/jpeg;base64,";
        if (!dataUrl.startsWith(prefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG profile photo");
        }
        byte[] input;
        try {
            input = Base64.getDecoder().decode(dataUrl.substring(prefix.length()));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The profile photo is not valid base64 data");
        }
        if (input.length == 0 || input.length > 1_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The profile photo must be smaller than 1 MB");
        }

        try (ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(input))) {
            if (stream == null) throw new IOException("Unsupported image");
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException("Unsupported image");
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096 || (long) width * height > 12_000_000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image dimensions are too large");
                }
                BufferedImage source = reader.read(0);
                if (source == null) throw new IOException("Unsupported image");
                double scale = Math.min(1d, 640d / Math.max(width, height));
                int nextWidth = Math.max(1, (int) Math.round(width * scale));
                int nextHeight = Math.max(1, (int) Math.round(height * scale));
                BufferedImage normalized = new BufferedImage(nextWidth, nextHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = normalized.createGraphics();
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(source, 0, 0, nextWidth, nextHeight, null);
                graphics.dispose();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!ImageIO.write(normalized, "jpeg", output) || output.size() > 1_000_000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The profile photo could not be processed");
                }
                return output.toByteArray();
            } finally {
                reader.dispose();
            }
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The profile photo is not a valid image");
        }
    }

    private AuthResponse tokenResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer", UserResponse.from(user));
    }

    private void requireBcryptLength(String password){
        if(password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password must be 72 bytes or fewer");
    }
}
