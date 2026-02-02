package com.falcon.booking.domain.exception.Passenger;

public class PassengerHasDifferentPassportNumberException extends RuntimeException {
    public PassengerHasDifferentPassportNumberException() {
        super("Passenger already has a different registered passport. Manual verification required.");
    }
}
