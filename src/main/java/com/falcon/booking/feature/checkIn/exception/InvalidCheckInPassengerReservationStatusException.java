package com.falcon.booking.feature.checkIn.exception;

import com.falcon.booking.common.enums.PassengerReservationStatus;

public class InvalidCheckInPassengerReservationStatusException extends RuntimeException {
    public InvalidCheckInPassengerReservationStatusException(PassengerReservationStatus status) {
        super("Check-In failed, the status of the reservation is " + status);
    }
}
