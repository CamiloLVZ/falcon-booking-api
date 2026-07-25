package com.falcon.booking.feature.auth.exception;

public class InvalidPasswordResetTokenException extends RuntimeException {
    public InvalidPasswordResetTokenException() {
        super("The password reset code is invalid or has expired.");
    }
}
