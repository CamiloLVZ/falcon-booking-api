package com.falcon.booking.feature.route.exception;

import com.falcon.booking.common.enums.RouteStatus;

public class InvalidRouteStatusForFlightGenerationException extends RuntimeException {
    public InvalidRouteStatusForFlightGenerationException(RouteStatus routeStatus)
    {
        super("Route with status " + routeStatus + " is not able to generate flights");
    }
}
