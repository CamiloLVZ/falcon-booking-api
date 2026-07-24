package com.falcon.booking.feature.reservation.exception;

public class PassengerAlreadyReservedFlightException extends RuntimeException {
    public PassengerAlreadyReservedFlightException(String passengerId, long flightId) {
        super("Passenger with id: " + passengerId + " has already a reservation on flight " + flightId);
    }
}
