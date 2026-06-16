package com.falcon.booking.feature.route.exception;

public class RouteSameOriginAndDestinationException extends RuntimeException {
    public RouteSameOriginAndDestinationException() {
        super("Route can not have same origin and destination");
    }
}
