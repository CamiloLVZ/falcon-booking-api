package com.falcon.booking.domain.exception.Reservation;

import com.falcon.booking.domain.valueobject.PassengerReservationStatus;

public class ReservationInvalidStatusChangeException extends RuntimeException {
    public ReservationInvalidStatusChangeException(PassengerReservationStatus initialStatus, PassengerReservationStatus finalStatus) {
        super("There is no posible change reservation status from "+initialStatus+" to "+finalStatus);
    }
}
