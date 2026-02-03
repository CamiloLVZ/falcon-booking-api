package com.falcon.booking.domain.exception.Reservation;

public class ReservationMustHavePassengersException extends RuntimeException {
    public ReservationMustHavePassengersException() {
        super("The request for reservation must have at least one passenger");
    }
}
