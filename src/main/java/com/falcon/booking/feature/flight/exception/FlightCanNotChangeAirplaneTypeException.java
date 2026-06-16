package com.falcon.booking.feature.flight.exception;

import com.falcon.booking.common.enums.FlightStatus;

public class FlightCanNotChangeAirplaneTypeException extends RuntimeException {
    public FlightCanNotChangeAirplaneTypeException(FlightStatus flightStatus) {

        super("Flight with status " + flightStatus + " is not allowed to change airplane type");
    }
}
