package com.falcon.booking.feature.route.exception;

public class RouteNotActiveException extends RuntimeException {
    public RouteNotActiveException(String flightNumber) {
        super("Route " + flightNumber + " is not active.");
    }
}
