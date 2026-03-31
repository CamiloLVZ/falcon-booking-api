package com.falcon.booking.domain.service;

import com.falcon.booking.web.dto.route.ResponseRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RouteActivationOrchestrator {

    private final RouteService routeService;
    private final FlightService flightService;

    @Autowired
    public RouteActivationOrchestrator(RouteService routeService, FlightService flightService) {
        this.routeService = routeService;
        this.flightService = flightService;
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        ResponseRouteDto responseRouteDto = routeService.activateRoute(flightNumber);
        flightService.startRouteFlightGeneration(flightNumber);

        return responseRouteDto;
    }
}
