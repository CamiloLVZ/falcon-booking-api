package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    flightGenerationService.startRouteFlightGeneration(flightNumber);
                }
            });
        } else {
            flightGenerationService.startRouteFlightGeneration(flightNumber);
        }

        return responseRouteDto;
    }
}
