package com.falcon.booking.feature.airport.exception;

public class AirportAlreadyExistsException extends RuntimeException {
    public AirportAlreadyExistsException(String iataCode) {
        super("Airport with code " + iataCode + " already exists");
    }
}
