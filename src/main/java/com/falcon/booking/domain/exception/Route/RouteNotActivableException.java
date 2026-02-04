package com.falcon.booking.domain.exception.Route;

public class RouteNotActivableException extends RuntimeException {
    public RouteNotActivableException(String message) {

        super("Route is not able to activate: " +message);
    }
}
