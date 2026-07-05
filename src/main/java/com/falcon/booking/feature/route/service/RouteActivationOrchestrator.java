package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RouteActivationOrchestrator {

    private final RouteCommandService routeCommandService;
    private final FlightGenerationService  flightGenerationService;

    @Autowired
    public RouteActivationOrchestrator(RouteCommandService routeCommandService, FlightGenerationService flightGenerationService) {
        this.routeCommandService = routeCommandService;
        this.flightGenerationService = flightGenerationService;
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        ResponseRouteDto responseRouteDto = routeCommandService.activateRoute(flightNumber);
        flightGenerationService.startRouteFlightGeneration(flightNumber);

        return responseRouteDto;
    }
}
