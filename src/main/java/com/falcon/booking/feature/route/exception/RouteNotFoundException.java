package com.falcon.booking.feature.route.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String flightNumber) {
        super("Route " + flightNumber + " not found");
    }
    public RouteNotFoundException(Long id) {
        super("Route with id " + id + " not found");
    }
}

