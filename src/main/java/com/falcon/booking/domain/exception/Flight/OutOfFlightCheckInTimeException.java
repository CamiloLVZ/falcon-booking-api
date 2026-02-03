package com.falcon.booking.domain.exception.Flight;

public class OutOfFlightCheckInTimeException extends RuntimeException {
    public OutOfFlightCheckInTimeException(Long id) {
        super("The flight " + id + " is not currently available for check-in.");
    }
}
