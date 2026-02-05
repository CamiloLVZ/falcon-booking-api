package com.falcon.booking.domain.exception.Passenger;

public class PassengerAlreadyExistsException extends RuntimeException {
    public PassengerAlreadyExistsException(String passportNumber) {
        super("There is a different passenger registered with passport number " + passportNumber + ". Manual action required.");
    }

    public PassengerAlreadyExistsException(String identificationNumber, String countryIsoCode) {
        super("Passenger with identification number " + identificationNumber + " already exists with country "+ countryIsoCode+ ". Manual action required.");
    }
}
