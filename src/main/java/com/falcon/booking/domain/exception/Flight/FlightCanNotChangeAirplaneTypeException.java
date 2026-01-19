package com.falcon.booking.domain.exception.Flight;

import com.falcon.booking.domain.valueobject.FlightStatus;

public class FlightCanNotChangeAirplaneTypeException extends RuntimeException {
    public FlightCanNotChangeAirplaneTypeException(FlightStatus flightStatus) {

        super("Flight with status " + flightStatus + " is not allowed to change airplane type");
    }
}
