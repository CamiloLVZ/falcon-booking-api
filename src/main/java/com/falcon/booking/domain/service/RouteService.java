package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.Route.*;
import com.falcon.booking.domain.mapper.RouteMapper;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.*;
import com.falcon.booking.persistence.specification.RouteSpecifications;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import com.falcon.booking.web.dto.route.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;

import java.util.List;
import java.util.Set;


@Service
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;
    private final RouteDayRepository routeDayRepository;
    private final RouteScheduleRepository routeScheduleRepository;
    private final RouteMapper routeMapper;
    private final FlightGenerationService flightGenerationService;
    private final AirplaneTypeService airplaneTypeService;
    private final AirportService airportService;

    @Autowired
    public RouteService(RouteRepository routeRepository, RouteDayRepository routeDayRepository, RouteScheduleRepository routeScheduleRepository, RouteMapper routeMapper, FlightGenerationService flightGenerationService, AirplaneTypeService airplaneTypeService, AirportService airportService) {
        this.routeRepository = routeRepository;
        this.routeDayRepository = routeDayRepository;
        this.routeScheduleRepository = routeScheduleRepository;
        this.routeMapper = routeMapper;
        this.flightGenerationService = flightGenerationService;
        this.airplaneTypeService = airplaneTypeService;
        this.airportService = airportService;
    }

    public RouteEntity getRouteEntity(String flightNumber){
        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);
        return routeRepository.findByFlightNumber(normalizedFlightNumber)
                .orElseThrow(()-> new RouteDoesNotExistException(normalizedFlightNumber));
    }

    @Transactional(readOnly = true)
    public List<RouteEntity> getAllRoutesByStatus(RouteStatus status) {
        return routeRepository.findAllByStatus(status);
    }

    @Transactional(readOnly = true)
    public ResponseRouteDto getRouteByFlightNumber(String flightNumber) {

        return routeMapper.toResponseDto(getRouteEntity(flightNumber));
    }

    @Transactional(readOnly = true)
    public List<ResponseRouteDto> getAllRoutes(String airportOrigin_iataCode,
                                        String airportDestination_iataCode,
                                        RouteStatus status) {

        String normalizedAirportOrigin_iataCode = StringNormalizer.normalize(airportOrigin_iataCode);
        String normalizedAirportDestination_iataCode = StringNormalizer.normalize(airportDestination_iataCode);

        Specification<RouteEntity> specification = Specification.allOf();
        specification = specification.and(RouteSpecifications.hasOriginIataCode(normalizedAirportOrigin_iataCode));
        specification = specification.and(RouteSpecifications.hasDestinationIataCode(normalizedAirportDestination_iataCode));
        specification = specification.and(RouteSpecifications.hasStatus(status));

        List<RouteEntity> entities = routeRepository.findAll(specification);
        return routeMapper.toResponseDto(entities);
    }

    @Transactional
    public ResponseRouteDto addRoute(CreateRouteDto createRouteDto) {

        if(routeRepository.existsByFlightNumber(createRouteDto.flightNumber()))
            throw new RouteAlreadyExistsException(createRouteDto.flightNumber());
        if(createRouteDto.airportOriginIataCode().equals(createRouteDto.airportDestinationIataCode()))
            throw new RouteSameOriginAndDestinationException();

        AirplaneTypeEntity airplaneType = airplaneTypeService.getAirplaneTypeEntity(createRouteDto.idDefaultAirplaneType());
        if(!airplaneType.isActive())
            throw new RouteAirplaneTypeIsNotActiveException(createRouteDto.idDefaultAirplaneType());

        AirportEntity airportOrigin = airportService.getAirportEntityByIataCode(createRouteDto.airportOriginIataCode());
        AirportEntity airportDestination = airportService.getAirportEntityByIataCode(createRouteDto.airportDestinationIataCode());

        RouteEntity entityToSave = routeMapper.toEntity(createRouteDto);

        entityToSave.setAirportOrigin(airportOrigin);
        entityToSave.setAirportDestination(airportDestination);
        entityToSave.setDefaultAirplaneType(airplaneType);
        entityToSave.markAsDraft();

        return routeMapper.toResponseDto(routeRepository.save(entityToSave));

    }

@Transactional
    public ResponseRouteDto updateRoute(String flightNumber, UpdateRouteDto updateRouteDto) {

        RouteEntity entityToUpdate = getRouteEntity(flightNumber);

        boolean hasOnlyDraftModifications = updateRouteDto.airportDestinationIataCode()!=null||updateRouteDto.airportOriginIataCode()!=null;

        if(hasOnlyDraftModifications && !entityToUpdate.isDraft()) {
            throw new RouteDraftInvalidUpdateException(flightNumber);
        }

        if (updateRouteDto.airportOriginIataCode() != null){
            AirportEntity airportOrigin = airportService.getAirportEntityByIataCode(updateRouteDto.airportOriginIataCode());
            entityToUpdate.setAirportOrigin(airportOrigin);
        }

        if (updateRouteDto.airportDestinationIataCode() != null){
            AirportEntity airportDestination = airportService.getAirportEntityByIataCode(updateRouteDto.airportDestinationIataCode());
           entityToUpdate.setAirportDestination(airportDestination);
        }

        if(entityToUpdate.getAirportOrigin().equals(entityToUpdate.getAirportDestination())){
            throw new RouteSameOriginAndDestinationException();
        }

        if(updateRouteDto.lengthMinutes()!=null)
            entityToUpdate.setLengthMinutes(updateRouteDto.lengthMinutes());

        if (updateRouteDto.idDefaultAirplaneType() != null){
            AirplaneTypeEntity airplaneType = airplaneTypeService.getAirplaneTypeEntity(updateRouteDto.idDefaultAirplaneType());
            entityToUpdate.setDefaultAirplaneType(airplaneType);
        }

        return routeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        RouteEntity routeEntity = getRouteEntity(flightNumber);
        routeEntity.activate();

        try {
            ResponseFlightsGeneratedDto result = flightGenerationService.generateFlightsForRoute(routeEntity);
            logger.info("Route {} activated with {} flights generated", flightNumber, result.flightsGenerated());
            return routeMapper.toResponseDto(routeEntity);

        } catch (Exception e) {
            logger.error("Route {} activated but failed flights generation: {}", flightNumber, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public ResponseRouteDto deactivateRoute(String flightNumber) {

        RouteEntity entityToUpdate = getRouteEntity(flightNumber);
        entityToUpdate.deactivate();

        return routeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public RouteWithSchedulesDto setRouteOperatingSchedules(String flightNumber, AddRouteScheduleRequestDto requestDto) {
        RouteEntity routeEntity = getRouteEntity(flightNumber);
        if (requestDto.weekDays() != null) {
            setRouteDays(routeEntity, requestDto.weekDays());
        }
        if(requestDto.schedules() != null) {
            setRouteSchedules(routeEntity, requestDto.schedules());
        }
        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());
    }

    public void setRouteDays (RouteEntity routeEntity, Set<DayOfWeek> days) {

        routeDayRepository.deleteAllByRoute(routeEntity);
        routeDayRepository.flush();
        routeEntity.updateWeekDays(days);
    }

    public void setRouteSchedules(RouteEntity routeEntity, Set<LocalTime> schedules){

        routeScheduleRepository.deleteAllByRoute(routeEntity);
        routeScheduleRepository.flush();
        routeEntity.updateSchedules(schedules);
    }

    @Transactional(readOnly = true)
    public RouteWithSchedulesDto getRouteWithSchedules(String flightNumber){

        RouteEntity routeEntity = getRouteEntity(flightNumber);
        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());

    }
}
