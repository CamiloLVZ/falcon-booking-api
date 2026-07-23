package com.falcon.booking.feature.airplaneType.exception;

public class InvalidSeatNumberException extends RuntimeException {

    public InvalidSeatNumberException(int seatNumber, int totalSeats) {
        super("Seat number " + seatNumber + " is invalid. Valid range: 1-" + totalSeats);
    }

}