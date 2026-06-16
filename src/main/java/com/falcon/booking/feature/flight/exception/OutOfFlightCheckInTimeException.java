package com.falcon.booking.feature.flight.exception;

public class OutOfFlightCheckInTimeException extends RuntimeException {
    public OutOfFlightCheckInTimeException(Long id) {
        super("The flight " + id + " is not currently available for check-in.");
    }
}
