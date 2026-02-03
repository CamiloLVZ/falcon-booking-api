package com.falcon.booking.domain.exception.Reservation;

import com.falcon.booking.domain.valueobject.PassengerReservationStatus;

public class InvalidCheckInPassengerReservationException extends RuntimeException {
    public InvalidCheckInPassengerReservationException(PassengerReservationStatus status) {
        super("Check-In failed, the status of the reservation is " + status);
    }
}
