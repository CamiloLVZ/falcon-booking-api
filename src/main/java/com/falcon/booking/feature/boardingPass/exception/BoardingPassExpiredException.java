package com.falcon.booking.feature.boardingPass.exception;

public class BoardingPassExpiredException extends RuntimeException {
    public BoardingPassExpiredException(String message) {
        super("The boarding pass has expired: "+ message);
    }
}
