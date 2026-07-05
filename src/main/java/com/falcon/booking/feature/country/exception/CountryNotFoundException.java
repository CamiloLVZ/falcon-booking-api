package com.falcon.booking.feature.country.exception;

public class CountryNotFoundException extends RuntimeException{

    public CountryNotFoundException(String isoCode) {
        super("Country with ISO code " + isoCode + " not found");
    }
}
