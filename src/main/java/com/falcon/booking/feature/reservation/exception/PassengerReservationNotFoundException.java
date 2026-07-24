package com.falcon.booking.feature.reservation.exception;

public class PassengerReservationNotFoundException extends RuntimeException {
    public PassengerReservationNotFoundException(Long id) {
        super("Passenger reservation with id " + id + " not found");
    }
}
