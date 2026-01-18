package com.falcon.booking.domain.exception.Flight;

public class FlightDoesNotExistException extends RuntimeException {
    public FlightDoesNotExistException(Long id) {

      super("The flight with id " + id + " does not exist");
    }
}
