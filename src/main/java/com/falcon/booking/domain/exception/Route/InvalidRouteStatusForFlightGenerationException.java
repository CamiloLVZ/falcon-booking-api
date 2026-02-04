package com.falcon.booking.domain.exception.Route;

import com.falcon.booking.domain.valueobject.RouteStatus;

public class InvalidRouteStatusForFlightGenerationException extends RuntimeException {
    public InvalidRouteStatusForFlightGenerationException(RouteStatus routeStatus)
    {
        super("A route with status " + routeStatus + " is not able to generate flights");
    }
}
