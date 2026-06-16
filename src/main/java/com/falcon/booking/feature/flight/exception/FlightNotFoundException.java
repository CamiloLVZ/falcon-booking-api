package com.falcon.booking.feature.flight.exception;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(Long id) {

      super("Flight with id " + id + " not found");
    }
}
