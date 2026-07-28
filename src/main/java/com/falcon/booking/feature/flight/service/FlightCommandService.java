package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightAlreadyExistsException;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeRescheduledException;
import com.falcon.booking.feature.flight.exception.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Slf4j
@Service
public class FlightCommandService {

    private final FlightRepository flightRepository;
    private final FlightQueryService flightQueryService;
    private final RouteQueryService routeQueryService;
    private final AirplaneTypeService airplaneTypeService;
    private final FlightMapper flightMapper;

    public FlightCommandService(FlightRepository flightRepository, FlightQueryService flightQueryService, RouteQueryService routeQueryService, AirplaneTypeService airplaneTypeService, FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.flightQueryService = flightQueryService;
        this.routeQueryService = routeQueryService;
        this.airplaneTypeService = airplaneTypeService;
        this.flightMapper = flightMapper;
    }

    @Transactional
    public ResponseFlightDto addFlight(CreateFlightDto createFlightDto) {
        RouteEntity route = routeQueryService.getRouteEntity(createFlightDto.routeFlightNumber());
        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDepartureDateTime = createFlightDto.departureDateTime().atZone(timezone).toOffsetDateTime();

        checkFlightDoesNotExist(route, offsetDepartureDateTime);

        FlightEntity entityToSave = new FlightEntity(route, route.getDefaultAirplaneType(), offsetDepartureDateTime, FlightStatus.SCHEDULED);
        entityToSave.setBasePriceEconomy(route.getBasePriceEconomy());
        entityToSave.setBasePriceFirstClass(route.getBasePriceFirstClass());

        FlightEntity entitySaved = flightRepository.save(entityToSave);
        log.info("Single flight generated for route {} with departure time {}", route.getFlightNumber(), offsetDepartureDateTime);
        return flightMapper.toDto(entitySaved);
    }

    private void checkFlightDoesNotExist(RouteEntity route, OffsetDateTime departureDateTime) {
        if (flightRepository.existsByRouteAndDepartureDateTime(route, departureDateTime))
            throw new FlightAlreadyExistsException(route.getFlightNumber(), departureDateTime);
    }


    @Transactional
    public ResponseFlightDto cancelFlight(Long id) {
        FlightEntity flightEntity = flightQueryService.getFlightEntity(id);
        flightEntity.cancel();
        log.info("Flight {} changed status to CANCELED", id);
        return flightMapper.toDto(flightRepository.save(flightEntity));
    }

    @Transactional
    public ResponseFlightDto rescheduleFLight(Long id, LocalDateTime newDepartureDateTime) {
        FlightEntity oldFlight = flightQueryService.getFlightEntity(id);
        RouteEntity route = oldFlight.getRoute();

        checkFlightCanBeRescheduled(oldFlight);

        ZoneId timezone = ZoneId.of(route.getAirportOrigin().getTimezone());
        OffsetDateTime offsetDepartureDateTime = newDepartureDateTime.atZone(timezone).toOffsetDateTime();

        if (flightRepository.existsByRouteAndDepartureDateTime(route, offsetDepartureDateTime))
            throw new FlightAlreadyExistsException(route.getFlightNumber(), offsetDepartureDateTime);

        oldFlight.cancel();
        FlightEntity newFlightEntity = new FlightEntity(route, route.getDefaultAirplaneType(), offsetDepartureDateTime, FlightStatus.SCHEDULED);
        log.info("Flight {} rescheduled. new departure: {}", id, newDepartureDateTime);
        return flightMapper.toDto(flightRepository.save(newFlightEntity));
    }

    private void checkFlightCanBeRescheduled(FlightEntity flight){
        if (!(flight.isScheduled() || flight.isCanceled()))
            throw new FlightCanNotBeRescheduledException(flight.getStatus());
    }

    @Transactional
    public ResponseFlightDto changeAirplaneType(Long id, Long idAirplaneType) {
        FlightEntity flightToUpdate = flightQueryService.getFlightEntity(id);

        checkFlightCanChangeAirplaneType(flightToUpdate);

        AirplaneTypeEntity airplaneTypeEntity = airplaneTypeService.getAirplaneTypeEntity(idAirplaneType);
        flightToUpdate.setAirplaneType(airplaneTypeEntity);
        log.info("Flight {} changed airplane type to {}", id, airplaneTypeEntity.getFullName());
        return flightMapper.toDto(flightRepository.save(flightToUpdate));
    }

    private void checkFlightCanChangeAirplaneType(FlightEntity flight) {
        if (!flight.isScheduled())
            throw new FlightCanNotChangeAirplaneTypeException(flight.getStatus());
    }

}
