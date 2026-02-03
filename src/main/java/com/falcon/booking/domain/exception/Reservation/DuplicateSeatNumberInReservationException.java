package com.falcon.booking.domain.exception.Reservation;

public class DuplicateSeatNumberInReservationException extends RuntimeException {
    public DuplicateSeatNumberInReservationException(int seatNumber) {

        super("The seat number " + seatNumber + " is duplicated in the reservation request");
    }
}
