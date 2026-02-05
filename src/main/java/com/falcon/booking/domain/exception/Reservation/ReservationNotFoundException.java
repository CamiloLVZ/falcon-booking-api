package com.falcon.booking.domain.exception.Reservation;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String number) {

      super("Reservation " + number + " not found");
    }
}
