package com.falcon.booking.feature.route.exception;

public class RouteHasNotSchedulesToGenerateFlightsException extends RuntimeException {
    public RouteHasNotSchedulesToGenerateFlightsException(String flightNumber) {

        super("The route "+flightNumber+" has not schedules to generate flights");
    }
}
