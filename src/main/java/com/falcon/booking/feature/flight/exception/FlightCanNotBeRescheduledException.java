package com.falcon.booking.feature.flight.exception;

import com.falcon.booking.common.enums.FlightStatus;

public class FlightCanNotBeRescheduledException extends RuntimeException {
    public FlightCanNotBeRescheduledException(FlightStatus flightStatus) {

        super("Flight with status " + flightStatus + " is not allowed to be rescheduled");
    }
}
