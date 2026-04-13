package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.DateToBeforeDateFromException;
import com.falcon.booking.domain.exception.Flight.FlightAlreadyExistsException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotBeRescheduledException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.domain.exception.Flight.FlightNotFoundException;
import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationAlreadyRunningException;
import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationNotFoundException;
import com.falcon.booking.domain.exception.Route.RouteNotActiveException;
import com.falcon.booking.domain.mapper.FlightGenerationMapper;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.specification.FlightSpecifications;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class FlightService {

    private final FlightGenerationMapper flightGenerationMapper;
    @Value("${app.flight.check-in.hours-before-to-start}")
    int checkInHoursBeforeToStart;
    @Value("${app.flight.check-in.hours-before-to-close}")
    int checkInHoursBeforeToClose;
    @Value("${app.flight.boarding.minutes-before-to-start}")
    int boardingMinutesBeforeToStart;
    @Value("${app.flight.boarding.minutes-before-to-close}")
    int boardingMinutesBeforeToClose;

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);


    private final FlightRepository flightRepository;
    private final RouteService routeService;
    private final AirplaneTypeService airplaneTypeService;
    private final FlightMapper flightMapper;
    private final AsyncFlightGenerationService asyncFlightGenerationService;
    private final FlightGenerationRepository flightGenerationRepository;

    @Autowired
    public FlightService(FlightRepository flightRepository, RouteService routeService, FlightMapper flightMapper, AirplaneTypeService airplaneTypeService, AsyncFlightGenerationService asyncFlightGenerationService, FlightGenerationRepository flightGenerationRepository, FlightGenerationMapper flightGenerationMapper) {
        this.flightRepository = flightRepository;
        this.routeService = routeService;
        this.flightMapper = flightMapper;
        this.airplaneTypeService = airplaneTypeService;
        this.asyncFlightGenerationService = asyncFlightGenerationService;
        this.flightGenerationRepository = flightGenerationRepository;
        this.flightGenerationMapper = flightGenerationMapper;
    }

    private OffsetDateTime toOffsetDateTime(LocalDate date, ZoneId zoneId) {
        if (date == null)
                return null;
        LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.MIN);
        return localDateTime.atZone(zoneId).toOffsetDateTime();
    }

    public FlightEntity getFlightEntity(Long id){
        return flightRepository.findById(id)
                .orElseThrow( () -> new FlightNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public ResponseFlightDto getFlightById(Long id) {
        FlightEntity flightEntity = getFlightEntity(id);

        return flightMapper.toDto(flightEntity);
    }

    public ResponseFlightsGenerationDto getFlightGeneration(Long id){
        FlightGenerationEntity entity = flightGenerationRepository.findById(id)
                .orElseThrow(()-> new FlightGenerationNotFoundException(id));

        return flightGenerationMapper.toDto(entity);
    }

    public List<ResponseFlightsGenerationDto> getAllFlightGenerations(){
        return flightGenerationMapper.toDto(flightGenerationRepository.findAll());
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
        logger.info("Single flight generated for route {} with departure time {}", route.getFlightNumber(), offsetDepartureDateTime);
        return flightMapper.toDto(entitySaved);
    }


    @Transactional(readOnly = true)
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
        logger.info("Flight {} changed status to CANCELED", id);
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
        logger.info("Flight {} rescheduled. new departure: {}", id, newDepartureDateTime);
        return flightMapper.toDto(flightRepository.save(newFlightEntity));
    }

    @Transactional
    public ResponseFlightDto changeAirplaneType(Long id, Long idAirplaneType){
        FlightEntity flightToUpdate = getFlightEntity(id);

        if(!flightToUpdate.isScheduled())
            throw new FlightCanNotChangeAirplaneTypeException(flightToUpdate.getStatus());

        AirplaneTypeEntity airplaneTypeEntity = airplaneTypeService.getAirplaneTypeEntity(idAirplaneType);
        flightToUpdate.setAirplaneType(airplaneTypeEntity);
        logger.info("Flight {} changed airplane type to {}", id, airplaneTypeEntity.getFullName());
        return flightMapper.toDto(flightRepository.save(flightToUpdate));
    }

    @Transactional(readOnly = true)
    public List<ResponseFlightDto> getAllFlightsByRouteAndDate(String flightNumber, LocalDate date) {

        RouteEntity routeEntity = routeService.getRouteEntity(flightNumber);
        if (!routeEntity.isActive())
            throw new RouteNotActiveException(routeEntity.getFlightNumber());

        ZoneId timezone = ZoneId.of(routeEntity.getAirportOrigin().getTimezone());

        OffsetDateTime startDateTime = date.atStartOfDay(timezone).toOffsetDateTime();
        OffsetDateTime endDateTime = date.atTime(LocalTime.MAX).atZone(timezone).toOffsetDateTime();

        return flightMapper.toDto(flightRepository.findAllByRouteAndDepartureDateTimeBetween(routeEntity, startDateTime, endDateTime));
    }

    public boolean updateFlightStatus(FlightEntity flight, OffsetDateTime now){
        OffsetDateTime departureDateTime = flight.getDepartureDateTime();
        OffsetDateTime checkInStart = departureDateTime.minusHours(checkInHoursBeforeToStart);
        OffsetDateTime checkInEnd = departureDateTime.minusHours(checkInHoursBeforeToClose);
        OffsetDateTime boardingStart = departureDateTime.minusMinutes(boardingMinutesBeforeToStart);
        OffsetDateTime boardingEnd = departureDateTime.minusMinutes(boardingMinutesBeforeToClose);

        boolean isInCheckInRange = !now.isBefore(checkInStart) && !now.isAfter(checkInEnd);
        boolean isInBoardingRange = !now.isBefore(boardingStart) && !now.isAfter(boardingEnd);

        flight.correctStatusByTime(now);
        if(now.isAfter(departureDateTime)) {
            flight.markAsCompleted();
            return true;
        }
        if(isInCheckInRange && !flight.isCheckInAvailable()) {
            flight.startCheckIn();
            return true;
        }
        if (isInBoardingRange && !flight.isInBoarding()) {
            flight.startBoarding();
            return true;
        }
        return false;
    }

    @Transactional
    public int updateFlightsStatus(){
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<FlightEntity> flightsToUpdate = flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED);

        int updatesCounter = 0;

        for(FlightEntity flight : flightsToUpdate){
            boolean updated = updateFlightStatus(flight, now);
            if(updated)
                updatesCounter++;
        }
        return updatesCounter;
    }

    public ResponseFlightsGenerationDto startGlobalFlightGeneration() {
        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());
            return flightGenerationMapper.toDto(generationSaved);

        } catch (DataIntegrityViolationException e) {
            throw new FlightGenerationAlreadyRunningException();
        }
    }

    public ResponseFlightsGenerationDto startRouteFlightGeneration(String flightNumber) {
        RouteEntity routeEntity = routeService.getRouteEntity(flightNumber);
        if(!routeEntity.isActive())
            throw new RouteNotActiveException(routeEntity.getFlightNumber());

        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startRouteGeneration(routeEntity.getId());
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());
            return flightGenerationMapper.toDto(generationSaved);

        } catch (DataIntegrityViolationException e) {
            throw new FlightGenerationAlreadyRunningException();
        }
    }

    public void startDailyFlightGeneration(LocalDate targetDate) {

        try {
            FlightGenerationEntity generation = FlightGenerationEntity.startDailyGeneration(targetDate);
            FlightGenerationEntity generationSaved = flightGenerationRepository.save(generation);
            asyncFlightGenerationService.executeGeneration(generationSaved.getId());

        } catch (DataIntegrityViolationException e) {
            throw new FlightGenerationAlreadyRunningException();
        }
    }

}

