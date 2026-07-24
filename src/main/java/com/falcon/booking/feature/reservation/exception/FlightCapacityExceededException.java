package com.falcon.booking.feature.reservation.exception;

public class FlightCapacityExceededException extends RuntimeException {
    public FlightCapacityExceededException(Long flightId) {
        super("The flight with id " + flightId + " has reached its maximum capacity.");
    }
}
