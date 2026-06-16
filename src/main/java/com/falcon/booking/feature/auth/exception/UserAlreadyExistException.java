package com.falcon.booking.feature.auth.exception;

public class UserAlreadyExistException extends RuntimeException {
    public UserAlreadyExistException(String email) {
        super("There is already a registered user with email " + email);
    }
}
