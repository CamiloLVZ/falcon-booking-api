package com.falcon.booking.feature.reservation.exception;

public class DuplicateSeatNumberInReservationException extends RuntimeException {
    public DuplicateSeatNumberInReservationException(int seatNumber) {

        super("The seat number " + seatNumber + " is duplicated in the reservation request");
    }
}
