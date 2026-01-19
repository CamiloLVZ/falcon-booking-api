package com.falcon.booking.domain.exception.Flight;

import com.falcon.booking.domain.valueobject.FlightStatus;

public class FlightCanNotBeRescheduledException extends RuntimeException {
    public FlightCanNotBeRescheduledException(FlightStatus flightStatus) {

        super("Flight with status " + flightStatus + " is not allowed to be rescheduled");
    }
}
