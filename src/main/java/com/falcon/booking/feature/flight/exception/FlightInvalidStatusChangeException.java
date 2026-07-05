package com.falcon.booking.feature.flight.exception;

import com.falcon.booking.common.enums.FlightStatus;

public class FlightInvalidStatusChangeException extends RuntimeException {
    public FlightInvalidStatusChangeException(FlightStatus initialStatus, FlightStatus finalStatus) {
        super("There is no posible change route status from "+initialStatus+" to "+finalStatus);
    }
}
