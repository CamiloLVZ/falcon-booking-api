package com.falcon.booking.feature.flight.exception;

public class FlightCanNotBeReservedException extends RuntimeException {
    public FlightCanNotBeReservedException(Long id) {
        super("Flight " + id + " is not able to make reservations");
    }
}
