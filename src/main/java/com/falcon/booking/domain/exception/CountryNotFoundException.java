package com.falcon.booking.domain.exception;

public class CountryNotFoundException extends RuntimeException{

    public CountryNotFoundException(String isoCode) {
        super("Country with ISO code " + isoCode + " not found");
    }
}
