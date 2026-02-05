package com.falcon.booking.domain.exception.Passenger;

public class PassengerNotFoundException extends RuntimeException {
    public PassengerNotFoundException(Long id) {
      super("Passenger with id " + id + " not found");
    }

  public PassengerNotFoundException(String passportNumber) {
    super("Passenger with passport number " + passportNumber + " not found");
  }

    public PassengerNotFoundException(String identificationNumber, String countryIsoCode) {
        super("Passenger with identification number " + identificationNumber + " and country "+ countryIsoCode+" not found");
    }

}
