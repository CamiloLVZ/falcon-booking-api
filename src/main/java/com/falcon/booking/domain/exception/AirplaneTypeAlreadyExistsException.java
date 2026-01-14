package com.falcon.booking.domain.exception;

public class AirplaneTypeAlreadyExistsException extends RuntimeException {
    public AirplaneTypeAlreadyExistsException(String producer, String model) {

        super("Duplicate entry: Airplane Type "+ producer + " - " + model + " is already registered.");
    }
}
