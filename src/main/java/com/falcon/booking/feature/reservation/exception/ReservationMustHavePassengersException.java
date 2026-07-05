package com.falcon.booking.feature.reservation.exception;

public class ReservationMustHavePassengersException extends RuntimeException {
    public ReservationMustHavePassengersException() {
        super("The request for reservation must have at least one passenger");
    }
}
