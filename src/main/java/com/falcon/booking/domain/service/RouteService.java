package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirportDoesNotExistException;
import com.falcon.booking.domain.exception.Route.*;
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
import com.falcon.booking.web.dto.Route.UpdateRouteDto;
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

        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);

        RouteEntity routeEntity = routeRepository.findByFlightNumber(normalizedFlightNumber).
                orElseThrow(()->new RouteDoesNotExistException(flightNumber));
        return routeMapper.toResponseDto(routeEntity);
    }

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
        entityToSave.setStatus(RouteStatus.DRAFT);

        return routeMapper.toResponseDto(routeRepository.save(entityToSave));

    }

@Transactional
    public ResponseRouteDto updateRoute(String flightNumber, UpdateRouteDto updateRouteDto) {
        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);

        RouteEntity entityToUpdate = routeRepository.findByFlightNumber(normalizedFlightNumber)
                .orElseThrow(()-> new RouteDoesNotExistException(flightNumber));

        boolean hasOnlyDraftModifications = updateRouteDto.airportDestinationIataCode()!=null||updateRouteDto.airportOriginIataCode()!=null;

        if(hasOnlyDraftModifications && entityToUpdate.getStatus()!=RouteStatus.DRAFT)
        {
            throw new RouteDraftInvalidUpdateException(flightNumber);
        }

        if (updateRouteDto.airportOriginIataCode() != null){
            AirportEntity airportOrigin = airportRepository.findByIataCode(updateRouteDto.airportOriginIataCode())
                    .orElseThrow(()->new AirportDoesNotExistException(updateRouteDto.airportOriginIataCode()));
            entityToUpdate.setAirportOrigin(airportOrigin);
        }

        if (updateRouteDto.airportDestinationIataCode() != null){
            AirportEntity airportDestination = airportRepository.findByIataCode(updateRouteDto.airportDestinationIataCode())
                            .orElseThrow(()->new AirportDoesNotExistException(updateRouteDto.airportDestinationIataCode()));
           entityToUpdate.setAirportDestination(airportDestination);
        }

        if(entityToUpdate.getAirportOrigin().getId().equals(entityToUpdate.getAirportDestination().getId())){
            throw new RouteSameOriginAndDestinationException();
        }

        if(updateRouteDto.lengthMinutes()!=null)
            entityToUpdate.setLengthMinutes(updateRouteDto.lengthMinutes());

        if (updateRouteDto.idDefaultAirplaneType() != null){
            AirplaneTypeEntity airplaneType = airplaneTypeRepository.findById(updateRouteDto.idDefaultAirplaneType())
                    .orElseThrow(()-> new AirplaneTypeDoesNotExistException(updateRouteDto.idDefaultAirplaneType()));
            entityToUpdate.setDefaultAirplaneType(airplaneType);
        }

        return routeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public ResponseRouteDto activateRoute(String flightNumber) {
        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);

        RouteEntity entityToUpdate = routeRepository.findByFlightNumber(normalizedFlightNumber)
                .orElseThrow(()-> new RouteDoesNotExistException(flightNumber));

        switch (entityToUpdate.getStatus()){
            case ACTIVE: break;

            case INACTIVE: entityToUpdate.setStatus(RouteStatus.ACTIVE);

            case DRAFT: {
                //TODO extra verification for DRAFT->ACTIVE
                entityToUpdate.setStatus(RouteStatus.ACTIVE);
            }
        }

        return routeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public ResponseRouteDto deactivateRoute(String flightNumber) {
        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);

        RouteEntity entityToUpdate = routeRepository.findByFlightNumber(normalizedFlightNumber)
                .orElseThrow(()-> new RouteDoesNotExistException(flightNumber));

        switch (entityToUpdate.getStatus()){
            case ACTIVE: { entityToUpdate.setStatus(RouteStatus.INACTIVE);
                break;
            }
            case INACTIVE: return routeMapper.toResponseDto(entityToUpdate);

            case DRAFT: throw new RouteInvalidStatusChangeException(RouteStatus.DRAFT, RouteStatus.INACTIVE);
        }

        return routeMapper.toResponseDto(entityToUpdate);
    }
}
