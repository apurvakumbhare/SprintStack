package com.sprintstack;

import com.sprintstack.entity.User;
import com.sprintstack.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {
    private static final String SIGNING_SECRET = "jwt-test-secret-with-at-least-32-characters";

    @Test
    void generatedTokenCarriesItsUsersEmailAndRejectsAnotherSigningKey() {
        JwtService issuer = service(SIGNING_SECRET, 60_000);
        String token = issuer.generateToken(new User("Test User", "jwt-user@example.test", "hash"));
        assertThat(issuer.extractEmail(token)).isEqualTo("jwt-user@example.test");

        JwtService otherIssuer = service("a-different-test-secret-with-32-characters", 60_000);
        assertThrows(JwtException.class, () -> otherIssuer.extractEmail(token));
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService service = service(SIGNING_SECRET, 1);
        String token = service.generateToken(new User("Test User", "expired@example.test", "hash"));
        Thread.sleep(25);
        assertThrows(JwtException.class, () -> service.extractEmail(token));
    }

    private static JwtService service(String secret, long expirationMs) {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", secret);
        ReflectionTestUtils.setField(service, "expirationMs", expirationMs);
        return service;
    }
}
