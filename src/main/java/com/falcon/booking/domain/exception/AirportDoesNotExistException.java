package com.falcon.booking.domain.exception;

public class AirportDoesNotExistException extends RuntimeException {
    public AirportDoesNotExistException(String iataCode) {
        super("The Airport with code " + iataCode + " does not exist");
    }
}
