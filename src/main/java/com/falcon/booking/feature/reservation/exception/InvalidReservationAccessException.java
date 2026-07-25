package com.falcon.booking.feature.reservation.exception;

public class InvalidReservationAccessException extends RuntimeException {
    public InvalidReservationAccessException() {
        super("The reservation number or contact email is invalid.");
    }
}
