package com.falcon.booking.feature.flight.exception;

public class OutOfFlightBoardingTimeException extends RuntimeException {
    public OutOfFlightBoardingTimeException(Long id) {
        super("The flight " + id + " is not currently available for boarding.");
    }
}
