package com.falcon.booking.feature.auth.web;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.falcon.booking.feature.auth.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityExceptionHandlerTest {

    private final SecurityExceptionHandler handler = new SecurityExceptionHandler();

    @Test
    void shouldHandleUserNotFound() {
        var response = handler.handleException(new UserNotFoundException("test@test.com"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("user-not-found");
    }

    @Test
    void shouldHandleUserAlreadyExist() {
        var response = handler.handleException(new UserAlreadyExistException("test@test.com"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("user-already-exists");
    }

    @Test
    void shouldHandleRoleNotFound() {
        var response = handler.handleException(new RoleNotFoundException("ADMIN"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("role-not-found");
    }

    @Test
    void shouldHandleRoleAlreadyExists() {
        var response = handler.handleException(new RoleAlreadyExistsException("ADMIN"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("role-already-exists");
    }

    @Test
    void shouldHandleBadCredentials() {
        var response = handler.handleException(new BadCredentialsException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().type()).isEqualTo("invalid-credentials");
    }

    @Test
    void shouldHandleJWTVerificationException() {
        var response = handler.handleException(new JWTVerificationException("test") {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().type()).isEqualTo("jwt-verification-failed");
    }

    @Test
    void shouldHandleAccessDenied() {
        var response = handler.handleException(new AccessDeniedException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().type()).isEqualTo("access-denied");
    }

    @Test
    void shouldHandleInvalidPasswordResetToken() {
        var response = handler.handleException(new InvalidPasswordResetTokenException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-password-reset-token");
    }

    @Test
    void shouldHandleDisabledException() {
        var response = handler.handleException(new DisabledException("Account disabled"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().type()).isEqualTo("user-disabled");
    }
}
