package com.falcon.booking.feature.passenger.exception;

public class PassengerHasDifferentPassportNumberException extends RuntimeException {
    public PassengerHasDifferentPassportNumberException() {
        super("Passenger already has a different registered passport. Manual verification required.");
    }
}
