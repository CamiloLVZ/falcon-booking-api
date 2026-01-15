package com.falcon.booking.domain.exception.Route;

public class RouteAlreadyExistsException extends RuntimeException {
    public RouteAlreadyExistsException(String flightNumber) {

        super("The route " + flightNumber + " is already registered.");
    }
}
