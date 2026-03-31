package com.falcon.booking.domain.exception.FlightGeneration;

public class FlightGenerationAlreadyRunningException extends RuntimeException {
    public FlightGenerationAlreadyRunningException() {
        super("There is already a flight generation running , try again later.");
    }
}
