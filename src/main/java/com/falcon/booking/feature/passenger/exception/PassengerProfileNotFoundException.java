package com.falcon.booking.feature.passenger.exception;

public class PassengerProfileNotFoundException extends RuntimeException {
    public PassengerProfileNotFoundException() {
        super("Passenger profile not found for current user");
    }
}
