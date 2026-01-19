package com.falcon.booking.domain.exception.Flight;

import com.falcon.booking.domain.valueobject.FlightStatus;

public class FlightInvalidStatusChangeException extends RuntimeException {
    public FlightInvalidStatusChangeException(FlightStatus initialStatus, FlightStatus finalStatus) {
        super("There is no posible change route status from "+initialStatus+" to "+finalStatus);
    }
}
