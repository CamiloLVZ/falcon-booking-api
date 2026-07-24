package com.falcon.booking.feature.boarding.exception;

import java.util.UUID;

public class BoardingPassNotFoundException extends RuntimeException {
    public BoardingPassNotFoundException(UUID token) {
        super("Boarding pass with token " + token + " not found");
    }
}
