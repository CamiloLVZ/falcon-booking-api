package com.falcon.booking.domain.exception.Route;

public class RouteDoesNotExistException extends RuntimeException {
    public RouteDoesNotExistException(Long id) {

        super("The route with id: "+ id +" does not exist");
    }

    public RouteDoesNotExistException(String flightNumber) {
        super("The route " + flightNumber + " does not exist");
    }
}

