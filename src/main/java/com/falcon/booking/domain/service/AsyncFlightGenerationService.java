package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationNotFoundException;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class AsyncFlightGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFlightGenerationService.class);

    private final FlightGenerationRepository flightGenerationRepository;
    private final TransactionalFlightGenerationService transactionalFlightGenerationService;

    @Autowired
    public AsyncFlightGenerationService(FlightGenerationRepository flightGenerationRepository, TransactionalFlightGenerationService transactionalFlightGenerationService) {
        this.flightGenerationRepository = flightGenerationRepository;
        this.transactionalFlightGenerationService = transactionalFlightGenerationService;
    }

    @Async("flightGenerationExecutor")
    public void executeGeneration(Long generationId){
        FlightGenerationEntity generation = flightGenerationRepository.findById(generationId)
                .orElseThrow(()-> new FlightGenerationNotFoundException(generationId));

        try {
            int totalGenerated=0;

            switch (generation.getType()){
                case GLOBAL:{
                    totalGenerated = transactionalFlightGenerationService.generateAllFlightsForAllRoutes();
                    generation.markAsCompleted(totalGenerated);
                    break;
                }
                case ROUTE:{
                    totalGenerated = transactionalFlightGenerationService.generateAllFlightsForRoute(generation.getIdRoute());
                    generation.markAsCompleted(totalGenerated);
                    break;
                }
                case DAILY:{
                    totalGenerated = transactionalFlightGenerationService.generateFlightsForAllRoutesAtHorizon();
                    generation.markAsCompleted(totalGenerated);
                    break;
                }
            }

            Duration duration = Duration.between(generation.getStartedAt(), generation.getFinishedAt());
            logger.info("{} flight generation completed. {} flights generated in {} seconds.", generation.getType(), totalGenerated, duration.toSeconds());

        }catch (Exception ex) {
            generation.markAsFailed();
            logger.error("Flight generation execution failed: {}", ex.getMessage());
        }finally {
            flightGenerationRepository.save(generation);
        }
    }

}