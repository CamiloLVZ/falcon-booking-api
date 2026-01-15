package com.falcon.booking.domain.exception.Route;

public class RouteAirplaneTypeIsNotActiveException extends RuntimeException {
    public RouteAirplaneTypeIsNotActiveException(String message) {
        super(message);
    }
    public RouteAirplaneTypeIsNotActiveException(Long id) {
        super("The Airplane Type with id "+id+" is not active, it can not be assigned a route.");
    }
}
