package com.falcon.booking.domain.exception.Route;

public class RouteNotActiveException extends RuntimeException {
    public RouteNotActiveException(String flightNumber) {
        super("Route " + flightNumber + " is not active.");
    }
}
