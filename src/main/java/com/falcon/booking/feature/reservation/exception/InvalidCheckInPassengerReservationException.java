package com.falcon.booking.feature.reservation.exception;

import com.falcon.booking.common.enums.PassengerReservationStatus;

public class InvalidCheckInPassengerReservationException extends RuntimeException {
    public InvalidCheckInPassengerReservationException(PassengerReservationStatus status) {
        super("Check-In failed, the status of the reservation is " + status);
    }
}
