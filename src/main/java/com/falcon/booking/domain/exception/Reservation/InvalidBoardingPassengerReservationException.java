package com.falcon.booking.domain.exception.Reservation;

import com.falcon.booking.domain.valueobject.PassengerReservationStatus;

public class InvalidBoardingPassengerReservationException extends RuntimeException {
    public InvalidBoardingPassengerReservationException(PassengerReservationStatus status) {
        super("Boarding failed, passenger reservation is " + status);
    }
}
