package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.route.dto.CreateRouteDto;
import com.falcon.booking.feature.route.dto.ResponseRouteDto;
import com.falcon.booking.feature.route.dto.UpdateRouteDto;
import com.falcon.booking.feature.route.exception.RouteAirplaneTypeIsNotActiveException;
import com.falcon.booking.feature.route.exception.RouteAlreadyExistsException;
import com.falcon.booking.feature.route.exception.RouteDraftInvalidUpdateException;
import com.falcon.booking.feature.route.exception.RouteSameOriginAndDestinationException;
import com.falcon.booking.feature.route.mapper.RouteMapper;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.RouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RouteCommandService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final AirplaneTypeService airplaneTypeService;
    private final AirportService airportService;
    private final RouteQueryService routeQueryService;

    public RouteCommandService(RouteRepository routeRepository,
                               RouteMapper routeMapper,
                               AirplaneTypeService airplaneTypeService,
                               AirportService airportService,
                               RouteQueryService routeQueryService) {
        this.routeRepository = routeRepository;
        this.routeMapper = routeMapper;
        this.airplaneTypeService = airplaneTypeService;
        this.airportService = airportService;
        this.routeQueryService = routeQueryService;
    }

    @Transactional
    public ResponseRouteDto addRoute(CreateRouteDto createRouteDto) {

        checkRouteDoesNotExist(createRouteDto);
        checkOriginDifferentFromDestination(createRouteDto);

        AirplaneTypeEntity airplaneType = airplaneTypeService.getAirplaneTypeEntity(createRouteDto.idDefaultAirplaneType());
        checkAirplaneTypeIsActive(airplaneType);


        AirportEntity airportOrigin = airportService.getAirportEntityByIataCode(createRouteDto.airportOriginIataCode());
        AirportEntity airportDestination = airportService.getAirportEntityByIataCode(createRouteDto.airportDestinationIataCode());

        RouteEntity entityToSave = routeMapper.toEntity(createRouteDto);
        entityToSave.setAirportOrigin(airportOrigin);
        entityToSave.setAirportDestination(airportDestination);
        entityToSave.setDefaultAirplaneType(airplaneType);
        entityToSave.markAsDraft();

        log.info("Route {} created: {} -> {}. {}", entityToSave.getFlightNumber(), airportOrigin.getIataCode(), airportDestination.getIataCode(), airplaneType.getFullName());
        return routeMapper.toResponseDto(routeRepository.save(entityToSave));
    }

    private void checkRouteDoesNotExist(CreateRouteDto createRouteDto){
        if (routeRepository.existsByFlightNumber(createRouteDto.flightNumber()))
            throw new RouteAlreadyExistsException(createRouteDto.flightNumber());
    }

    private void checkOriginDifferentFromDestination(CreateRouteDto createRouteDto) {
        if (createRouteDto.airportOriginIataCode().equals(createRouteDto.airportDestinationIataCode()))
            throw new RouteSameOriginAndDestinationException();
    }

    private void checkAirplaneTypeIsActive(AirplaneTypeEntity airplaneType) {
        if (!airplaneType.isActive())
            throw new RouteAirplaneTypeIsNotActiveException(airplaneType.getId());
    }

    @Transactional
    public ResponseRouteDto updateRoute(String flightNumber, UpdateRouteDto updateRouteDto) {
        RouteEntity entityToUpdate = routeQueryService.getRouteEntity(flightNumber);
        checkRouteIsDraftForModifications(updateRouteDto, entityToUpdate);

        if (updateRouteDto.airportOriginIataCode() != null) {
            AirportEntity airportOrigin = airportService.getAirportEntityByIataCode(updateRouteDto.airportOriginIataCode());
            entityToUpdate.setAirportOrigin(airportOrigin);
        }

        if (updateRouteDto.airportDestinationIataCode() != null) {
            AirportEntity airportDestination = airportService.getAirportEntityByIataCode(updateRouteDto.airportDestinationIataCode());
            entityToUpdate.setAirportDestination(airportDestination);
        }

        checkOriginDifferentFromDestination(entityToUpdate);

        if (updateRouteDto.durationMinutes() != null)
            entityToUpdate.setDurationMinutes(updateRouteDto.durationMinutes());

        if (updateRouteDto.idDefaultAirplaneType() != null) {
            AirplaneTypeEntity airplaneType = airplaneTypeService.getAirplaneTypeEntity(updateRouteDto.idDefaultAirplaneType());
            entityToUpdate.setDefaultAirplaneType(airplaneType);
        }

        if (updateRouteDto.basePriceEconomy() != null)
            entityToUpdate.setBasePriceEconomy(updateRouteDto.basePriceEconomy());

        if (updateRouteDto.basePriceFirstClass() != null)
            entityToUpdate.setBasePriceFirstClass(updateRouteDto.basePriceFirstClass());

        log.info("Route number {} was updated", entityToUpdate.getFlightNumber());
        return routeMapper.toResponseDto(entityToUpdate);
    }

    private void checkRouteIsDraftForModifications(UpdateRouteDto updateRouteDto, RouteEntity entityToUpdate) {
        boolean hasDraftModifications =
                updateRouteDto.airportOriginIataCode() != null && !updateRouteDto.airportOriginIataCode().equals(entityToUpdate.getAirportOrigin().getIataCode())
                        || updateRouteDto.airportDestinationIataCode() != null && !updateRouteDto.airportDestinationIataCode().equals(entityToUpdate.getAirportDestination().getIataCode());
        if (hasDraftModifications && !entityToUpdate.isDraft())
            throw new RouteDraftInvalidUpdateException(entityToUpdate.getFlightNumber());
    }

    private void checkOriginDifferentFromDestination(RouteEntity entity) {
        if (entity.getAirportOrigin().equals(entity.getAirportDestination()))
            throw new RouteSameOriginAndDestinationException();
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        RouteEntity routeEntity = routeQueryService.getRouteEntity(flightNumber);
        routeEntity.activate();
        log.info("Route {} activated.", routeEntity.getFlightNumber());
        return routeMapper.toResponseDto(routeEntity);
    }

    @Transactional
    public ResponseRouteDto deactivateRoute(String flightNumber) {
        RouteEntity entityToUpdate = routeQueryService.getRouteEntity(flightNumber);
        entityToUpdate.deactivate();
        log.info("Route {} deactivated", entityToUpdate.getFlightNumber());
        return routeMapper.toResponseDto(entityToUpdate);
    }
}
