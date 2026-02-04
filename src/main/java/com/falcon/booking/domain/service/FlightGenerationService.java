package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.Route.InvalidRouteStatusForFlightGenerationException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class FlightGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(FlightGenerationService.class);

    @Value("${app.generation.horizon-days}")
    int flightGenerationDaysHorizon;

    @Value("${app.generation.minimum-hours-before-departure}")
    int minimumHoursBeforeDeparture;

    private final FlightRepository flightRepository;

    @Autowired
    public FlightGenerationService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public ResponseFlightsGeneratedDto generateFlightsForRoute(RouteEntity route) {
        if(!route.getStatus().equals(RouteStatus.ACTIVE)) {
            throw new InvalidRouteStatusForFlightGenerationException(route.getStatus());
        }

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());
        LocalDate currentDate = LocalDate.now(timeZoneId);
        LocalDate horizonDate = currentDate.plusDays(flightGenerationDaysHorizon);
        int totalGenerated = generateFlightsForRouteInRange(route, currentDate, horizonDate);

        return new ResponseFlightsGeneratedDto(
                route.getFlightNumber(),
                totalGenerated,
                currentDate,
                horizonDate
        );
    }

    @Transactional
    public int generateFlightsForRouteAtDate(RouteEntity route, LocalDate targetDate) {
        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());

        if (!route.getOperatingDays().contains(targetDate.getDayOfWeek())) {
            return 0;
        }

        List<FlightEntity> flights = generateFlightsBySchedules(
                route.getOperatingSchedules(),
                targetDate,
                timeZoneId,
                route
        );

        if (!flights.isEmpty()) {
            flightRepository.saveAll(flights);
        }

        return flights.size();
    }

    private int generateFlightsForRouteInRange(RouteEntity route, LocalDate startDate, LocalDate endDate) {

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());
        Set<DayOfWeek> routeDays = route.getOperatingDays();
        Set<LocalTime> routeSchedules = route.getOperatingSchedules();

        List<FlightEntity> allFlightsToSave = new ArrayList<>();
        LocalDate dateIterator = startDate;

        while (dateIterator.isBefore(endDate)) {
            if (routeDays.contains(dateIterator.getDayOfWeek())) {
                List<FlightEntity> flightsForDay = generateFlightsBySchedules(routeSchedules, dateIterator, timeZoneId, route);
                allFlightsToSave.addAll(flightsForDay);
            }
            dateIterator = dateIterator.plusDays(1);
        }

        if (!allFlightsToSave.isEmpty()) {
            flightRepository.saveAll(allFlightsToSave);
        }

        return allFlightsToSave.size();
    }

    private List<FlightEntity> generateFlightsBySchedules(Set<LocalTime> routeSchedules, LocalDate date, ZoneId timeZoneId, RouteEntity route) {
        OffsetDateTime minimumDepartureTime = OffsetDateTime.now().plusHours(minimumHoursBeforeDeparture);

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
                flightEntities.add(new FlightEntity(
                        route,
                        route.getDefaultAirplaneType(),
                        departureDateTime,
                        FlightStatus.SCHEDULED
                ));
            }
        }

        return flightEntities;
    }
}