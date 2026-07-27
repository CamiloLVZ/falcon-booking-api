package com.falcon.booking.feature.auth.web;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.auth.exception.RoleAlreadyExistsException;
import com.falcon.booking.feature.auth.exception.RoleNotFoundException;
import com.falcon.booking.feature.auth.exception.UserAlreadyExistException;
import com.falcon.booking.feature.auth.exception.UserNotFoundException;
import com.falcon.booking.feature.auth.exception.InvalidPasswordResetTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class SecurityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Error> handleException(UserNotFoundException exception) {
        Error error = new Error("user-not-found", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<Error> handleException(UserAlreadyExistException exception) {
        Error error = new Error("user-already-exists", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Error> handleException(RoleNotFoundException exception) {
        Error error = new Error("role-not-found", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(RoleAlreadyExistsException exception) {
        Error error = new Error("role-already-exists", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Error> handleException(DisabledException exception) {
        Error error = new Error("user-disabled", "User account is disabled");
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Error> handleException(BadCredentialsException exception) {
        Error error = new Error("invalid-credentials", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<Error> handleException(JWTVerificationException exception) {
        Error error = new Error("jwt-verification-failed", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Error> handleException(AccessDeniedException exception) {
        Error error = new Error("access-denied", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidPasswordResetTokenException.class)
    public ResponseEntity<Error> handleException(InvalidPasswordResetTokenException exception) {
        Error error = new Error("invalid-password-reset-token", exception.getMessage());
        log.debug(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
