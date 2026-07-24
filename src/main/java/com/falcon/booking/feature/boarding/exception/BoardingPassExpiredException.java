package com.falcon.booking.feature.boarding.exception;

public class BoardingPassExpiredException extends RuntimeException {
    public BoardingPassExpiredException(String message) {
        super("The boarding pass has expired: "+ message);
    }
}
