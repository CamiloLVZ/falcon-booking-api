package com.falcon.booking.feature.route.exception;

public class RouteAlreadyExistsException extends RuntimeException {
    public RouteAlreadyExistsException(String flightNumber) {

        super("Route " + flightNumber + " is already registered.");
    }
}
