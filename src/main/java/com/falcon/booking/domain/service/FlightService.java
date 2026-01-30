package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.DateToBeforeDateFromException;
import com.falcon.booking.domain.exception.Flight.FlightAlreadyExistsException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotBeRescheduledException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.domain.exception.Flight.FlightDoesNotExistException;
import com.falcon.booking.domain.exception.Route.RouteNotActiveException;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.specification.FlightSpecifications;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class FlightService {

    @Value("${app.generation.horizon-days}")
    int flightGenerationDaysHorizon;

    @Value("${app.flight.boarding.window-hours}")
    int hoursBeforeFlightStartsBoarding;

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);


    private final FlightRepository flightRepository;
    private final RouteService routeService;
    private final AirplaneTypeService airplaneTypeService;
    private final FlightMapper flightMapper;
    private final FlightGenerationService flightGenerationService;
    @Autowired
    public FlightService(FlightRepository flightRepository, RouteService routeService, FlightMapper flightMapper, AirplaneTypeService airplaneTypeService, FlightGenerationService flightGenerationService) {
        this.flightRepository = flightRepository;
        this.routeService = routeService;
        this.flightMapper = flightMapper;
        this.airplaneTypeService = airplaneTypeService;
        this.flightGenerationService = flightGenerationService;
    }

    private OffsetDateTime toOffsetDateTime(LocalDate date, ZoneId zoneId) {
        if (date == null)
                return null;
        LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIN);
        return localDateTime.atZone(zoneId).toOffsetDateTime();
    }

    public FlightEntity getFlightEntity(Long id){
        return flightRepository.findById(id)
                .orElseThrow( () -> new FlightDoesNotExistException(id));
    }

    public ResponseFlightDto getFlightById(Long id) {
        FlightEntity flightEntity = getFlightEntity(id);

        return flightMapper.toDto(flightEntity);
    }


    @Transactional
    public ResponseFlightDto addFlight(CreateFlightDto createFlightDto) {

        RouteEntity route = routeService.getRouteEntity(createFlightDto.routeFlightNumber());
        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDepartureDateTime= createFlightDto.departureDateTime().atZone(timezone).toOffsetDateTime();

        if(flightRepository.existsByRouteAndDepartureDateTime(route, offsetDepartureDateTime))
            throw new FlightAlreadyExistsException(route.getFlightNumber(), offsetDepartureDateTime);

        FlightEntity entityToSave = new FlightEntity(route, route.getDefaultAirplaneType(),
                                                offsetDepartureDateTime, FlightStatus.SCHEDULED);

       FlightEntity entitySaved = flightRepository.save(entityToSave);

       return flightMapper.toDto(entitySaved);
    }


    @Transactional
    public List<ResponseFlightDto> getAllFlights(String flightNumber, FlightStatus flightStatus, LocalDate dateFrom, LocalDate dateTo) {

        RouteEntity route = routeService.getRouteEntity(flightNumber);

        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDateTimeFrom = toOffsetDateTime(dateFrom, timezone);
        OffsetDateTime offsetDateTimeTo = toOffsetDateTime(dateTo, timezone);

        Specification<FlightEntity> spec = Specification.allOf();
        spec = spec.and(FlightSpecifications.hasRoute(route));
        spec = spec.and(FlightSpecifications.hasStatus(flightStatus));
        spec = spec.and(FlightSpecifications.hasDateStart(offsetDateTimeFrom));
        spec = spec.and(FlightSpecifications.hasDateEnd(offsetDateTimeTo));

        return flightMapper.toDto(flightRepository.findAll(spec));
    }

    @Transactional
    public ResponseFlightDto cancelFlight(Long id) {
        FlightEntity flightEntity = getFlightEntity(id);
        flightEntity.cancel();
        return flightMapper.toDto(flightEntity);
    }

    @Transactional
    public ResponseFlightDto rescheduleFLight(Long id, LocalDateTime newDepartureDateTime) {
        FlightEntity oldFlight = getFlightEntity(id);
        RouteEntity route = oldFlight.getRoute();

        if(!(oldFlight.isScheduled() || oldFlight.isCanceled()))
            throw new FlightCanNotBeRescheduledException(oldFlight.getStatus());

        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDepartureDateTime = newDepartureDateTime.atZone(timezone).toOffsetDateTime();

        if(flightRepository.existsByRouteAndDepartureDateTime(route, offsetDepartureDateTime))
            throw new FlightAlreadyExistsException(route.getFlightNumber(), offsetDepartureDateTime);

        oldFlight.cancel();
        FlightEntity newFlightEntity = new FlightEntity(route, route.getDefaultAirplaneType(),
                offsetDepartureDateTime, FlightStatus.SCHEDULED);

        return flightMapper.toDto(flightRepository.save(newFlightEntity));
    }

    @Transactional
    public ResponseFlightDto changeAirplaneType(Long id, Long idAirplaneType){
        FlightEntity flightToUpdate = getFlightEntity(id);

        if(!flightToUpdate.isScheduled())
            throw new FlightCanNotChangeAirplaneTypeException(flightToUpdate.getStatus());

        AirplaneTypeEntity airplaneTypeEntity = airplaneTypeService.getAirplaneTypeEntity(idAirplaneType);
        flightToUpdate.setAirplaneType(airplaneTypeEntity);
        return flightMapper.toDto(flightRepository.save(flightToUpdate));
    }

    public List<ResponseFlightDto> getAllFlightsByRouteAndDates(String flightNumber, LocalDate dateFrom, LocalDate dateTo) {

        if(dateTo.isBefore(dateFrom)) throw new DateToBeforeDateFromException();
        RouteEntity routeEntity = routeService.getRouteEntity(flightNumber);
        if (routeEntity.getStatus() != RouteStatus.ACTIVE)
            throw new RouteNotActiveException(routeEntity.getFlightNumber());

        ZoneId timezone = ZoneId.of(routeEntity.getAirportOrigin().getTimezone());

        OffsetDateTime offsetDateTimeFrom = toOffsetDateTime(dateFrom, timezone);
        OffsetDateTime offsetDateTimeTo = toOffsetDateTime(dateTo, timezone);

        return flightMapper.toDto(flightRepository.findAllByRouteAndDepartureDateTimeBetween(routeEntity, offsetDateTimeFrom, offsetDateTimeTo));
    }

    @Transactional
    public void updateFlightsStatus(){
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime latestDepartureForBoarding = now.plusHours(hoursBeforeFlightStartsBoarding);

        List<FlightEntity> flightsToStartBoarding = flightRepository.findAllFlightsToStartBoarding(FlightStatus.SCHEDULED, latestDepartureForBoarding);
        List<FlightEntity> flightsToComplete = flightRepository.findAllFlightsToComplete(FlightStatus.BOARDING, now);

        for(FlightEntity flight : flightsToStartBoarding){
            flight.startBoarding();
        }
        for(FlightEntity flight : flightsToComplete){
            flight.markAsComplete();
        }
    }

    public List<ResponseFlightsGeneratedDto> generateAllFlightsForAllRoutes() {
        logger.info("Starting flights generation for all he active routes");

        List<ResponseFlightsGeneratedDto> results = new ArrayList<>();
        List<RouteEntity> routeEntities = routeService.getAllRoutesByStatus(RouteStatus.ACTIVE);

        int successCount = 0;
        int failCount = 0;

        for (RouteEntity routeEntity : routeEntities) {
            try {

                ResponseFlightsGeneratedDto result = flightGenerationService.generateFlightsForRoute(routeEntity);
                results.add(result);
                successCount++;

                logger.debug("Route {}: {} flights generated", routeEntity.getFlightNumber(), result.flightsGenerated());

            } catch (Exception e) {
                failCount++;
                logger.error("Error at flight generation for route {}: {}", routeEntity.getFlightNumber(), e.getMessage());

                ZoneId timeZoneId = ZoneId.of(routeEntity.getAirportOrigin().getTimezone());
                LocalDate currentDate = LocalDate.now(timeZoneId);
                results.add(new ResponseFlightsGeneratedDto(
                        routeEntity.getFlightNumber(),
                        0,
                        currentDate,
                        currentDate
                ));
            }
        }
        logger.info("Flights generation completed: {} success, {} failed for {} rroutes", successCount, failCount, routeEntities.size());

        return results;
    }

    public ResponseFlightsGeneratedDto generateAllFlightsForRoute(String flightNumber) {
        RouteEntity route = routeService.getRouteEntity(flightNumber);

        return flightGenerationService.generateFlightsForRoute(route);
    }

    public void generateFlightsForAllRoutesAtHorizon() {
        logger.info("Starting daily flights generation");

        List<RouteEntity> routeEntities = routeService.getAllRoutesByStatus(RouteStatus.ACTIVE);
        int processedCount = 0;
        int skippedCount = 0;
        int errorCount = 0;

        for (RouteEntity routeEntity : routeEntities) {
            try {
                ZoneId timeZoneId = ZoneId.of(routeEntity.getAirportOrigin().getTimezone());
                LocalDate currentDate = LocalDate.now(timeZoneId);
                LocalDate targetDate = currentDate.plusDays(flightGenerationDaysHorizon);

                int generated = flightGenerationService.generateFlightsForRouteAtDate(routeEntity, targetDate);

                if (generated > 0) {
                    processedCount++;
                    logger.debug("Route {}: {} flights generated for {}", routeEntity.getFlightNumber(), generated, targetDate);
                } else {
                    skippedCount++;
                }

            } catch (Exception e) {
                errorCount++;
                logger.error("Error at flights generations for route {} at horizon date: {}", routeEntity.getFlightNumber(), e.getMessage());
            }
        }

        logger.info("Daily flights generation completed: {} processed, {} skipped, {} errors", processedCount, skippedCount, errorCount);
    }
}

