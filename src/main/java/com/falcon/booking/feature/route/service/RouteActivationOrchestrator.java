package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RouteActivationOrchestrator {

    private final RouteService routeService;
    private final FlightGenerationService  flightGenerationService;

    @Autowired
    public RouteActivationOrchestrator(RouteService routeService, FlightGenerationService flightGenerationService) {
        this.routeService = routeService;
        this.flightGenerationService = flightGenerationService;
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        ResponseRouteDto responseRouteDto = routeService.activateRoute(flightNumber);
        flightGenerationService.startRouteFlightGeneration(flightNumber);

        return responseRouteDto;
    }
}
