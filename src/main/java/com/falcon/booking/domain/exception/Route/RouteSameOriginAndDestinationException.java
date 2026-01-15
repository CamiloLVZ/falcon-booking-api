package com.falcon.booking.domain.exception.Route;

public class RouteSameOriginAndDestinationException extends RuntimeException {
    public RouteSameOriginAndDestinationException(String message) {
        super(message);
    }

    public RouteSameOriginAndDestinationException() {
        super("A route can not have same origin and destination");
    }

}
