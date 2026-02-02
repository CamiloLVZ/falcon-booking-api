package com.falcon.booking.domain.exception.Passenger;

public class PassengerDoesNotExistException extends RuntimeException {
    public PassengerDoesNotExistException(Long id) {
      super("Passenger with id " + id + " does not exist");
    }

  public PassengerDoesNotExistException(String passportNumber) {
    super("Passenger with passport number " + passportNumber + " does not exist");
  }

    public PassengerDoesNotExistException(String identificationNumber, String countryIsoCode) {
        super("Passenger with identification number " + identificationNumber + " and country "+ countryIsoCode+" does not exist");
    }

}
