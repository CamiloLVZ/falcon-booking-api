package com.falcon.booking.domain.exception.Reservation;

public class SeatNumberAlreadyTakenException extends RuntimeException {
    public SeatNumberAlreadyTakenException(Integer seatNumber, Long id)
    {
        super("Seat number " + seatNumber + " in flight with id "+ id + " is already taken");
    }
}
