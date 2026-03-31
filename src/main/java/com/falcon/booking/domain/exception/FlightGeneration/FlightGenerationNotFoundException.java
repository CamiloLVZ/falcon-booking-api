package com.falcon.booking.domain.exception.FlightGeneration;

public class FlightGenerationNotFoundException extends RuntimeException {
    public FlightGenerationNotFoundException(Long id) {

      super("Flight generation with id " + id + " not found");
    }
}
