package com.falcon.booking.feature.reservation.exception;

public class PassengerNotFoundInReservationException extends RuntimeException {
    public PassengerNotFoundInReservationException(String identificationNumber, String countryIsoCode, String reservationNumber) {

        super("Passenger with identification "+countryIsoCode + " " + identificationNumber + " not found in reservation " + reservationNumber);
    }
}
