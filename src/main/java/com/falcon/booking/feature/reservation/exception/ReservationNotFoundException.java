package com.falcon.booking.feature.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String number) {

      super("Reservation " + number + " not found");
    }
}
