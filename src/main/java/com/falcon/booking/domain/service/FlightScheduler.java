package com.falcon.booking.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class FlightScheduler {

    private static final Logger logger = LoggerFactory.getLogger(FlightScheduler.class);
    private final FlightService flightService;

    @Autowired
    public FlightScheduler(FlightService flightService) {
        this.flightService = flightService;
    }

    @Scheduled(fixedRateString = "${app.flight.status.update-rate-ms:60000}")
    public void updateFlightsStatus() {
        logger.debug("Checking for flights status updates");
        try{
            int flightsUpdated = flightService.updateFlightsStatus();
            if(flightsUpdated > 0)
                logger.info("flights status updated: {}", flightsUpdated);
        }catch (Exception e){
            logger.error("Exception at flight status update: {}",e.getMessage());
        }
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void generateFlightsForHorizonDay(){
        logger.info("Starting daily flights generation");
        flightService.generateFlightsForAllRoutesAtHorizon();
    }

}
