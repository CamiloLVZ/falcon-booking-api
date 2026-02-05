package com.falcon.booking.domain.exception.Route;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String flightNumber) {
        super("Route " + flightNumber + " not found");
    }
}

