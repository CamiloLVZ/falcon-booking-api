package com.falcon.booking.domain.exception.Route;

public class RouteAirplaneTypeIsNotActiveException extends RuntimeException {
    public RouteAirplaneTypeIsNotActiveException(Long id) {
        super("Airplane Type with id "+id+" is not active, can not be assigned to a route.");
    }
}
