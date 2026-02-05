package com.falcon.booking.domain.exception;

public class AirportNotFoundException extends RuntimeException {
    public AirportNotFoundException(String iataCode) {
        super("Airport with code " + iataCode + " not found");
    }

    public AirportNotFoundException(Long id) {
        super("Airport with id: " + id + " not found");
    }
}
