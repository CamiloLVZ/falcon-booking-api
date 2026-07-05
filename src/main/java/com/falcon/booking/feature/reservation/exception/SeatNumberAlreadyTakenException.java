package com.falcon.booking.feature.reservation.exception;

public class SeatNumberAlreadyTakenException extends RuntimeException {
    public SeatNumberAlreadyTakenException(Integer seatNumber, Long id)
    {
        super("Seat number " + seatNumber + " in flight with id "+ id + " is already taken");
    }
}
