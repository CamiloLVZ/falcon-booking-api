package com.falcon.booking.feature.flightGeneration.exception;

public class FlightGenerationNotFoundException extends RuntimeException {
    public FlightGenerationNotFoundException(Long id) {

      super("Flight generation with id " + id + " not found");
    }
}
