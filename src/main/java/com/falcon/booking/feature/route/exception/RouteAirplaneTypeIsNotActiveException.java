package com.falcon.booking.feature.route.exception;

public class RouteAirplaneTypeIsNotActiveException extends RuntimeException {
    public RouteAirplaneTypeIsNotActiveException(Long id) {
        super("Airplane Type with id "+id+" is not active.");
    }
}
