package com.falcon.booking.feature.flight.exception;

import java.time.OffsetDateTime;

public class FlightAlreadyExistsException extends RuntimeException {
    public FlightAlreadyExistsException(String flightNumber, OffsetDateTime departureDateTime) {

      super("There is already an existing flight for the route " + flightNumber + " at "+departureDateTime);
    }
}
