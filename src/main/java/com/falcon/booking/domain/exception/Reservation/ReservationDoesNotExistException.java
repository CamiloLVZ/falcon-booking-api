package com.falcon.booking.domain.exception.Reservation;

public class ReservationDoesNotExistException extends RuntimeException {
    public ReservationDoesNotExistException(String number) {

      super("The reservation " + number + " does not exist");
    }
}
