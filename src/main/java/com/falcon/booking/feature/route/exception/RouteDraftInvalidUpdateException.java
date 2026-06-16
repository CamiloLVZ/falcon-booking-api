package com.falcon.booking.feature.route.exception;

public class RouteDraftInvalidUpdateException extends RuntimeException {
    public RouteDraftInvalidUpdateException(String flightNumber) {
        super("The Route "+flightNumber+" is not a draft, can not change origin or destination");
    }

}
