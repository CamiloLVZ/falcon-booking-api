package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirportDoesNotExistException;
import com.falcon.booking.domain.exception.Route.RouteAirplaneTypeIsNotActiveException;
import com.falcon.booking.domain.exception.Route.RouteAlreadyExistsException;
import com.falcon.booking.domain.exception.Route.RouteDoesNotExistException;
import com.falcon.booking.domain.exception.Route.RouteSameOriginAndDestinationException;
import com.falcon.booking.domain.mapper.RouteMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.AirplaneTypeRepository;
import com.falcon.booking.persistence.repository.AirportRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import com.falcon.booking.persistence.specification.RouteSpecifications;
import com.falcon.booking.web.dto.Route.CreateRouteDto;
import com.falcon.booking.web.dto.Route.ResponseRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final AirplaneTypeRepository airplaneTypeRepository;
    private final RouteMapper routeMapper;

    @Autowired
    public RouteService(RouteRepository routeRepository, AirportRepository airportRepository, AirplaneTypeRepository airplaneTypeRepository, RouteMapper routeMapper) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.airplaneTypeRepository = airplaneTypeRepository;
        this.routeMapper = routeMapper;
    }

   public ResponseRouteDto getRouteByFlightNumber(String flightNumber) {
        RouteEntity routeEntity = routeRepository.findByFlightNumber(flightNumber).
                orElseThrow(()->new RouteDoesNotExistException(flightNumber));
        return routeMapper.toResponseDto(routeEntity);
    }

    public List<ResponseRouteDto> getAllRoutes(String airportOrigin_iataCode,
                                        String airportDestination_iataCode,
                                        RouteStatus status) {

        Specification<RouteEntity> specification = Specification.allOf();

        specification = specification.and(RouteSpecifications.hasOriginIataCode(airportOrigin_iataCode));
        specification = specification.and(RouteSpecifications.hasDestinationIataCode(airportDestination_iataCode));
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

        AirplaneTypeEntity airplaneType = airplaneTypeRepository.findById(createRouteDto.idDefaultAirplaneType())
                .orElseThrow(()-> new AirplaneTypeDoesNotExistException(createRouteDto.idDefaultAirplaneType()));

        if(!airplaneType.getStatus().equals(AirplaneTypeStatus.ACTIVE))
            throw new RouteAirplaneTypeIsNotActiveException(createRouteDto.idDefaultAirplaneType());

        AirportEntity airportOrigin = airportRepository.findByIataCode(createRouteDto.airportOriginIataCode())
                .orElseThrow(()->new AirportDoesNotExistException(createRouteDto.airportOriginIataCode()));

        AirportEntity airportDestination = airportRepository.findByIataCode(createRouteDto.airportDestinationIataCode())
                .orElseThrow(()->new AirportDoesNotExistException(createRouteDto.airportDestinationIataCode()));

        RouteEntity entityToSave = routeMapper.toEntity(createRouteDto);

        entityToSave.setAirportOrigin(airportOrigin);
        entityToSave.setAirportDestination(airportDestination);
        entityToSave.setDefaultAirplaneType(airplaneType);
        entityToSave.setStatus(RouteStatus.ACTIVE);

        return routeMapper.toResponseDto(routeRepository.save(entityToSave));

    }


}
