package com.falcon.booking.feature.boardingPass.exception;

public class BoardingPassAlreadyBoardedException extends RuntimeException {
    public BoardingPassAlreadyBoardedException(String message) {
        super("The boarding pass has already used for boarding: "+ message);
    }
}
