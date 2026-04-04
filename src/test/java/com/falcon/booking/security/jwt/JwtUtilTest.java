package com.falcon.booking.security.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JwtUtilTest {

    private JwtUtil createJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", "test-secret-key-1234567890");
        ReflectionTestUtils.setField(jwtUtil, "issuerName", "falcon-booking-tests");
        ReflectionTestUtils.setField(jwtUtil, "expirationMillis", 60000L);
        jwtUtil.init();
        return jwtUtil;
    }

    @DisplayName("Should generate and extract payload from token")
    @Test
    void shouldGenerateAndExtractPayload_tokenLifecycle() {
        JwtUtil jwtUtil = createJwtUtil();
        JwtPayload expectedPayload = new JwtPayload(1L, "client@test.com", List.of("CLIENT", "ADMIN"));

        String token = jwtUtil.generateToken(expectedPayload);
        JwtPayload extractedPayload = jwtUtil.extractPayload(token);

        assertThat(token).isNotBlank();
        assertThat(extractedPayload.userId()).isEqualTo(1L);
        assertThat(extractedPayload.email()).isEqualTo("client@test.com");
        assertThat(extractedPayload.roles()).containsExactly("CLIENT", "ADMIN");
    }

    @DisplayName("Should throw JWTVerificationException when token is invalid")
    @Test
    void shouldThrowException_extractPayload() {
        JwtUtil jwtUtil = createJwtUtil();

        assertThrows(JWTVerificationException.class,
                () -> jwtUtil.extractPayload("invalid-token-value"));
    }
}
