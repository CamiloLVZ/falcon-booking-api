package com.falcon.booking.domain.exception.Flight;

public class FlightCanNotBeReservedException extends RuntimeException {
    public FlightCanNotBeReservedException(Long id) {
        super("Flight " + id + " is not able to make reservations");
    }
}
