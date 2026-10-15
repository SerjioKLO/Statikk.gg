package com.statikk.api_gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private JwtValidator jwtValidator;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator();
        ReflectionTestUtils.setField(jwtValidator, "jwtSecret", secret);
    }

    private String generateValidToken(String subject, String email, Long userId) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claims(Map.of("email", email, "userId", userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Should successfully validate a valid JWT token")
    void shouldValidateValidToken() {
        String token = generateValidToken("player1", "player1@statikk.gg", 100L);
        assertThat(jwtValidator.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should extract user details from valid token")
    void shouldExtractClaims() {
        String token = generateValidToken("player1", "player1@statikk.gg", 100L);
        assertThat(jwtValidator.extractUserId(token)).isEqualTo("100");
        assertThat(jwtValidator.extractEmail(token)).isEqualTo("player1@statikk.gg");
    }

    @Test
    @DisplayName("Should reject invalid or malformed token")
    void shouldRejectInvalidToken() {
        assertThat(jwtValidator.validateToken("invalid.token.string")).isFalse();
    }
}
