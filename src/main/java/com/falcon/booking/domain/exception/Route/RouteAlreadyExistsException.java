package com.falcon.booking.domain.exception.Route;

public class RouteAlreadyExistsException extends RuntimeException {
    public RouteAlreadyExistsException(String flightNumber) {

        super("Route " + flightNumber + " is already registered.");
    }
}
