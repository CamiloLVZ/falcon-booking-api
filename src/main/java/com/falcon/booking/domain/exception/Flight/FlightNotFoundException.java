package com.falcon.booking.domain.exception.Flight;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException(Long id) {

      super("Flight with id " + id + " not found");
    }
}
