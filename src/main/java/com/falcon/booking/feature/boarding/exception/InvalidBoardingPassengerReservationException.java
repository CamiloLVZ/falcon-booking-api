package com.falcon.booking.feature.boarding.exception;

import com.falcon.booking.common.enums.PassengerReservationStatus;

public class InvalidBoardingPassengerReservationException extends RuntimeException {
    public InvalidBoardingPassengerReservationException(PassengerReservationStatus status) {
        super("Boarding failed, passenger reservation is " + status);
    }
}
