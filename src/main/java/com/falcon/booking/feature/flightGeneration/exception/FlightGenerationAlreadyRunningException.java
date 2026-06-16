package com.falcon.booking.feature.flightGeneration.exception;

public class FlightGenerationAlreadyRunningException extends RuntimeException {
    public FlightGenerationAlreadyRunningException() {
        super("There is already a flight generation running , try again later.");
    }
}
