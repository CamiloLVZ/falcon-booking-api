package com.falcon.booking.feature.route.exception;

public class RouteNotActivableException extends RuntimeException {
    public RouteNotActivableException(String message) {

        super("Route is not able to activate: " +message);
    }
}
