package com.falcon.booking.feature.flight.scheduler;

import com.falcon.booking.feature.flight.service.FlightStatusService;
import com.falcon.booking.feature.flightGeneration.service.FlightGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;


@Slf4j
@Component
public class FlightScheduler {
    private final FlightStatusService flightStatusService;
    private final FlightGenerationService flightGenerationService;

    @Autowired
    public FlightScheduler(FlightStatusService flightStatusService, FlightGenerationService flightGenerationService) {
        this.flightStatusService = flightStatusService;
        this.flightGenerationService = flightGenerationService;
    }

    @Scheduled(fixedRateString = "${app.flight.status.update-rate-ms:60000}")
    public void updateFlightsStatus() {
        log.debug("Checking for flights status updates");
        try{
            int flightsUpdated = flightStatusService.updateFlightsStatus();
            if(flightsUpdated > 0)
                log.info("flights status updated: {}", flightsUpdated);
        }catch (Exception e){
            log.error("Exception at flight status update: {}",e.getMessage());
        }
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void generateFlightsForHorizonDay(){
        log.info("Starting daily flights generation");
        flightGenerationService.startDailyFlightGeneration(LocalDate.now());
    }

}
