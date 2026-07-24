package com.falcon.booking.feature.flightGeneration.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationPartialFailureException;
import com.falcon.booking.feature.route.exception.RouteNotFoundException;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionalFlightGenerationService {

    @Value("${app.generation.horizon-days}")
    int flightGenerationDaysHorizon;

    @Value("${app.generation.minimum-hours-before-departure}")
    int minimumHoursBeforeDeparture;

    @Value("${app.generation.batch-size}")
    int batchSize;

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
        log.info("Starting flights generation for all active routes");
        List<Long> routeIds = routeRepository.findIdsByStatus(RouteStatus.ACTIVE);

        List<CompletableFuture<Integer>> futuresFlightsGenerated = routeIds.stream()
                .map((routeId) -> CompletableFuture.supplyAsync(() -> {
                            try {
                                return generateAllFlightsForRoute(routeId);
                            } catch (Exception e) {
                                throw new CompletionException(new RouteGenerationException(routeId, e));
                            }
                        }, flightGenerationExecutor)
                )
                .toList();

        int totalGenerated = 0;
        List<Long> failedRouteIds = new ArrayList<>();

        for (CompletableFuture<Integer> future : futuresFlightsGenerated) {
            try {
                totalGenerated += future.join();
            } catch (CompletionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RouteGenerationException routeGenerationException) {
                    failedRouteIds.add(routeGenerationException.routeId());
                    log.error("Error generating flights for route {}. {}", routeGenerationException.routeId(), routeGenerationException.getCause().getMessage());
                } else {
                    log.error("Unexpected error waiting for route generation result. {}", e.getMessage());
                }
            }
        }

        if (!failedRouteIds.isEmpty()) {
            log.error("Flights generation finished with errors: {} flights generated, {} failed routes",
                    totalGenerated, failedRouteIds.size());
            throw new FlightGenerationPartialFailureException(failedRouteIds);
        }

        log.info("Flights generation completed: {} flights in {} routes",
                totalGenerated, routeIds.size());

        return totalGenerated;
    }

    private int generateFlightsForRouteInRange(RouteEntity route, LocalDate startDate, LocalDate endDate) {

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());

        Set<DayOfWeek> routeDays = route.getOperatingDays();
        Set<LocalTime> routeSchedules = route.getOperatingSchedules();

        OffsetDateTime minDeparture = OffsetDateTime.now(timeZoneId).plusHours(minimumHoursBeforeDeparture);

        OffsetDateTime startDateTime = startDate.atStartOfDay().atZone(timeZoneId).toOffsetDateTime();
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay().atZone(timeZoneId).toOffsetDateTime();

        List<OffsetDateTime> existingDeparturesList = flightRepository.findExistingDepartureTimesInRange(route.getId(), startDateTime, endDateTime);
        Set<Instant> existingDepartures = toInstantSet(existingDeparturesList);

        List<FlightEntity> batch = new ArrayList<>(batchSize);

        int totalGenerated = 0;
        LocalDate dateIterator = startDate;

        while (!dateIterator.isAfter(endDate)) {
            if (routeDays.contains(dateIterator.getDayOfWeek())) {

                for (LocalTime time : routeSchedules) {
                    OffsetDateTime departure = dateIterator.atTime(time).atZone(timeZoneId).toOffsetDateTime();
                    Instant departureInstant = departure.toInstant();

                    if (departure.isBefore(minDeparture))
                        continue;

                    if (existingDepartures.contains(departureInstant))
                        continue;

                    FlightEntity flight = new FlightEntity(route, route.getDefaultAirplaneType(), departure, FlightStatus.SCHEDULED);
                    flight.setBasePriceEconomy(route.getBasePriceEconomy());
                    flight.setBasePriceFirstClass(route.getBasePriceFirstClass());
                    batch.add(flight);
                    existingDepartures.add(departureInstant);

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

        List<OffsetDateTime> departureTimes = routeSchedules.stream()
                .map(time -> LocalDateTime.of(date, time).atZone(timeZoneId).toOffsetDateTime())
                .filter(departureDateTime -> departureDateTime.isAfter(minimumDepartureTime))
                .toList();

        if (departureTimes.isEmpty()) {
            return List.of();
        }

        List<OffsetDateTime> existingDeparturesList = flightRepository.findExistingDepartureTimes(route, departureTimes);
        Set<Instant> existingDepartures = toInstantSet(existingDeparturesList);

        List<FlightEntity> flightEntities = new ArrayList<>();
        for (OffsetDateTime departureDateTime : departureTimes) {
            Instant departureInstant = departureDateTime.toInstant();
            if (!existingDepartures.contains(departureInstant)) {
                flightEntities.add(buildFlightEntity(route, departureDateTime));
                existingDepartures.add(departureInstant);
            }
        }
        return flightEntities;
    }

    private FlightEntity buildFlightEntity(RouteEntity route, OffsetDateTime departureDateTime) {
        FlightEntity flight = new FlightEntity(
                route,
                route.getDefaultAirplaneType(),
                departureDateTime,
                FlightStatus.SCHEDULED
        );
        flight.setBasePriceEconomy(route.getBasePriceEconomy());
        flight.setBasePriceFirstClass(route.getBasePriceFirstClass());
        return flight;
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
                log.error("Error generating flights for route {} at horizon date. {}", routeId, e.getMessage());
            }
        }
        log.info("Daily flights generation completed: {} processed, {} skipped, {} errors", processedCount, skippedCount, errorCount);

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

    private Set<Instant> toInstantSet(List<OffsetDateTime> departures) {
        return departures.stream()
                .map(OffsetDateTime::toInstant)
                .collect(Collectors.toSet());
    }

    private static class RouteGenerationException extends RuntimeException {
        private final Long routeId;

        private RouteGenerationException(Long routeId, Throwable cause) {
            super(cause);
            this.routeId = routeId;
        }

        public Long routeId() {
            return routeId;
        }
    }

}
