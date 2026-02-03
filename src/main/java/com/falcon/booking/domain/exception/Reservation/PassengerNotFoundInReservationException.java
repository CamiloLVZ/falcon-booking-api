package com.falcon.booking.domain.exception.Reservation;

public class PassengerNotFoundInReservationException extends RuntimeException {
    public PassengerNotFoundInReservationException(String identificationNumber, String countryIsoCode, String reservationNumber) {

        super("The passenger with identification "+countryIsoCode + " " + identificationNumber + " was not found in the reservation " + reservationNumber);
    }
}
