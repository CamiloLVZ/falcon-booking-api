package com.falcon.booking.feature.reservation.exception;

import com.falcon.booking.common.enums.PassengerReservationStatus;

public class ReservationInvalidStatusChangeException extends RuntimeException {
    public ReservationInvalidStatusChangeException(PassengerReservationStatus initialStatus, PassengerReservationStatus finalStatus) {
        super("There is no posible change reservation status from "+initialStatus+" to "+finalStatus);
    }
}
