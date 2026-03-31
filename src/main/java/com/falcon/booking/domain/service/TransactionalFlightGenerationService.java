package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.Route.RouteNotFoundException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class TransactionalFlightGenerationService {

    @Value("${app.generation.horizon-days}")
    int flightGenerationDaysHorizon;

    @Value("${app.generation.minimum-hours-before-departure}")
    int minimumHoursBeforeDeparture;

    @Value("${app.generation.batch-size}")
    int batchSize;

    private static final Logger logger = LoggerFactory.getLogger(TransactionalFlightGenerationService.class);

    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final FlightBatchPersistenceService flightBatchPersistenceService;
    private final Executor flightGenerationExecutor;

    public TransactionalFlightGenerationService(FlightRepository flightRepository, RouteRepository routeRepository, FlightBatchPersistenceService flightBatchPersistenceService, Executor flightGenerationExecutor) {
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.flightBatchPersistenceService = flightBatchPersistenceService;
        this.flightGenerationExecutor = flightGenerationExecutor;
    }

    public int generateAllFlightsForRoute(Long id) {
        RouteEntity route = routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id));

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());
        LocalDate currentDate = LocalDate.now(timeZoneId);
        LocalDate horizonDate = currentDate.plusDays(flightGenerationDaysHorizon);

        return generateFlightsForRouteInRange(route, currentDate, horizonDate);
    }

    public int generateAllFlightsForAllRoutes() {
        logger.info("Starting flights generation for all active routes");
        List<Long> routeIds = routeRepository.findIdsByStatus(RouteStatus.ACTIVE);

        List<CompletableFuture<Integer>> futuresFlightsGenerated = routeIds.stream()
                .map((routeId) -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return generateAllFlightsForRoute(routeId);
                            } catch (Exception e) {
                                logger.error("Error generating flights for route {}, {}", routeId, e.getMessage());
                                return 0;
                            }
                        }, flightGenerationExecutor)
                )
                .toList();

        int totalGenerated = futuresFlightsGenerated.stream()
                .mapToInt(CompletableFuture::join)
                .sum();

        logger.info("Flights generation completed: {} flights in {} routes",
                totalGenerated, routeIds.size());

        return totalGenerated;
    }

    private int generateFlightsForRouteInRange(RouteEntity route, LocalDate startDate, LocalDate endDate) {

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());

        Set<DayOfWeek> routeDays = route.getOperatingDays();
        Set<LocalTime> routeSchedules = route.getOperatingSchedules();

        OffsetDateTime minDeparture = OffsetDateTime.now(timeZoneId).plusHours(minimumHoursBeforeDeparture);

        OffsetDateTime startDateTime = startDate.atStartOfDay().atZone(timeZoneId).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.atStartOfDay().atZone(timeZoneId).toOffsetDateTime();

        List<OffsetDateTime> existingDeparturesList = flightRepository.findExistingDepartureTimesInRange(route.getId(), startDateTime, endDateTime);
        Set<OffsetDateTime> existingDepartures = new HashSet<>(existingDeparturesList);

        List<FlightEntity> batch = new ArrayList<>(batchSize);

        int totalGenerated = 0;
        LocalDate dateIterator = startDate;

        while (!dateIterator.isAfter(endDate)) {
            if (routeDays.contains(dateIterator.getDayOfWeek())) {

                for (LocalTime time : routeSchedules) {
                    OffsetDateTime departure = dateIterator.atTime(time).atZone(timeZoneId).toOffsetDateTime();

                    if (departure.isBefore(minDeparture))
                        continue;

                    if (existingDepartures.contains(departure))
                        continue;

                    batch.add(new FlightEntity(route, route.getDefaultAirplaneType(), departure, FlightStatus.SCHEDULED));

                    if (batch.size() >= batchSize) {
                        flightBatchPersistenceService.saveBatch(batch);
                        totalGenerated += batch.size();
                        batch.clear();
                    }
                }
            }
            dateIterator = dateIterator.plusDays(1);
        }

        if (!batch.isEmpty()) {
            flightBatchPersistenceService.saveBatch(batch);
            totalGenerated += batch.size();
        }

        return totalGenerated;
    }

    private List<FlightEntity> generateFlightsBySchedules(Set<LocalTime> routeSchedules, LocalDate date, ZoneId timeZoneId, RouteEntity route) {
        OffsetDateTime minimumDepartureTime = OffsetDateTime.now(timeZoneId).plusHours(minimumHoursBeforeDeparture);

        List<OffsetDateTime> departureTimes = new ArrayList<>();
        for (LocalTime time : routeSchedules) {
            LocalDateTime datetime = LocalDateTime.of(date, time);
            OffsetDateTime departureDateTime = datetime.atZone(timeZoneId).toOffsetDateTime();
            if(departureDateTime.isAfter(minimumDepartureTime)) {
                departureTimes.add(departureDateTime);
            }

        }

        List<OffsetDateTime> existingDeparturesList = flightRepository.findExistingDepartureTimes(route, departureTimes);
        Set<OffsetDateTime> existingDepartures = new HashSet<>();
        for(OffsetDateTime departureDateTime : existingDeparturesList) {
            existingDepartures.add(departureDateTime.atZoneSameInstant(timeZoneId).toOffsetDateTime());
        }

        List<FlightEntity> flightEntities = new ArrayList<>();
        for (OffsetDateTime departureDateTime : departureTimes) {

            if (!existingDepartures.contains(departureDateTime)) {
                flightEntities.add(new FlightEntity(route, route.getDefaultAirplaneType(),
                        departureDateTime, FlightStatus.SCHEDULED));
            }
        }
        return flightEntities;
    }

    public int generateFlightsForRouteAtHorizon(Long routeId) {

        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());

        LocalDate currentDate = LocalDate.now(timeZoneId);
        LocalDate targetDate = currentDate.plusDays(flightGenerationDaysHorizon);

        return generateFlightsForRouteAtDate(route, targetDate);
    }

    public int generateFlightsForAllRoutesAtHorizon() {

        List<Long> routeIds = routeRepository.findIdsByStatus(RouteStatus.ACTIVE);

        int processedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;
        int totalGenerated = 0;

        for (Long routeId : routeIds) {
            try {
                int generated = generateFlightsForRouteAtHorizon(routeId);
                totalGenerated += generated;

                if (generated > 0)
                    processedCount++;
                else
                    skippedCount++;

            } catch (Exception e) {
                errorCount++;
                logger.error("Error generating flights for route {} at horizon date. {}", routeId, e.getMessage());
            }
        }
        logger.info("Daily flights generation completed: {} processed, {} skipped, {} errors", processedCount, skippedCount, errorCount);

        return totalGenerated;
    }

    public int generateFlightsForRouteAtDate(RouteEntity route, LocalDate targetDate) {

        if (!route.getOperatingDays().contains(targetDate.getDayOfWeek()))
            return 0;


        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());

        List<FlightEntity> flights = generateFlightsBySchedules(
                route.getOperatingSchedules(),
                targetDate, timeZoneId, route
        );

        if (!flights.isEmpty())
            flightBatchPersistenceService.saveBatch(flights);

        return flights.size();
    }

}
