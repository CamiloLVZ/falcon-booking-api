package com.falcon.booking.feature.country.exception;

public class CountryAlreadyExistsException extends RuntimeException {

    public CountryAlreadyExistsException(String field, String value) {
        super("Country with " + field + " '" + value + "' already exists");
    }
}
