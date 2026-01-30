package com.falcon.booking.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class FlightScheduler {

    private final FlightService flightService;

    @Autowired
    public FlightScheduler(FlightService flightService) {
        this.flightService = flightService;
    }

    @Scheduled(fixedRateString = "${app.flight.status.update-rate-ms:60000}")
    public void updateFlightsStatus() {
        flightService.updateFlightsStatus();
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void generateFlightsForHorizonDay(){
        flightService.generateFlightsForAllRoutesAtHorizon();
    }

}
