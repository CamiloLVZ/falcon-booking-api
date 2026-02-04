package com.falcon.booking.domain.exception.Route;

public class RouteSameOriginAndDestinationException extends RuntimeException {
    public RouteSameOriginAndDestinationException() {
        super("Route can not have same origin and destination");
    }
}
