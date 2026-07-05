package com.falcon.booking.feature.flightGeneration.exception;

import java.util.List;

public class FlightGenerationPartialFailureException extends RuntimeException {

    public FlightGenerationPartialFailureException(List<Long> failedRouteIds) {
        super("Flight generation failed for routes: " + failedRouteIds);
    }
}
