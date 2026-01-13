package com.falcon.booking.domain.exception;

public class CountryDoesNotExistException extends RuntimeException{

    public CountryDoesNotExistException(String isoCode) {
        super("Country with ISO code " + isoCode + " does not exist");
    }
}
