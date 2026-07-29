package com.falcon.booking.feature.reservation.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String number) {
      super("Reservation " + number + " not found");
    }

    public ReservationNotFoundException(Long id) {
      super("Reservation with id " + id + " not found");
    }
}
