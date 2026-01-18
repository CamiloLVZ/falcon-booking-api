package com.falcon.booking.domain.service;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirportDoesNotExistException;
import com.falcon.booking.domain.exception.Route.*;
import com.falcon.booking.domain.mapper.RouteMapper;
import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.*;
import com.falcon.booking.persistence.specification.RouteSpecifications;
import com.falcon.booking.web.dto.route.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final AirplaneTypeRepository airplaneTypeRepository;
    private final RouteDayRepository routeDayRepository;
    private final RouteScheduleRepository routeScheduleRepository;
    private final RouteMapper routeMapper;

    @Autowired
    public RouteService(RouteRepository routeRepository, AirportRepository airportRepository, AirplaneTypeRepository airplaneTypeRepository, RouteDayRepository routeDayRepository, RouteScheduleRepository routeScheduleRepository, RouteMapper routeMapper) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.airplaneTypeRepository = airplaneTypeRepository;
        this.routeDayRepository = routeDayRepository;
        this.routeScheduleRepository = routeScheduleRepository;
        this.routeMapper = routeMapper;
    }

    public RouteEntity getRouteEntity(String flightNumber){
        String normalizedFlightNumber = StringNormalizer.normalize(flightNumber);
        return routeRepository.findByFlightNumber(normalizedFlightNumber)
                .orElseThrow(()-> new RouteDoesNotExistException(normalizedFlightNumber));
    }

   public ResponseRouteDto getRouteByFlightNumber(String flightNumber) {

        return routeMapper.toResponseDto(getRouteEntity(flightNumber));
    }

    public List<RouteEntity> getAllRoutesByStatus(RouteStatus status) {
        return routeRepository.findAllByStatus(status);
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

        RouteEntity entityToUpdate = getRouteEntity(flightNumber);

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
        RouteEntity entityToUpdate = getRouteEntity(flightNumber);

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

        RouteEntity entityToUpdate = getRouteEntity(flightNumber);

        switch (entityToUpdate.getStatus()){
            case ACTIVE: { entityToUpdate.setStatus(RouteStatus.INACTIVE);
                break;
            }
            case INACTIVE: return routeMapper.toResponseDto(entityToUpdate);

            case DRAFT: throw new RouteInvalidStatusChangeException(RouteStatus.DRAFT, RouteStatus.INACTIVE);
        }

        return routeMapper.toResponseDto(entityToUpdate);
    }

    @Transactional
    public RouteWithSchedulesDto setRouteDays(String flightNumber, AddRouteDaysRequestDto dto){

        RouteEntity routeEntity = getRouteEntity(flightNumber);

        routeDayRepository.deleteAllByRoute(routeEntity);
        routeDayRepository.flush();

        routeEntity.updateWeekDays(dto.weekDays());

        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());
    }

    @Transactional
    public RouteWithSchedulesDto setRouteSchedules(String flightNumber, AddRouteScheduleRequestDto dto){
        RouteEntity routeEntity = getRouteEntity(flightNumber);

        routeScheduleRepository.deleteAllByRoute(routeEntity);
        routeScheduleRepository.flush();

        routeEntity.updateSchedules(dto.schedules());

        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());
    }

    @Transactional
    public RouteWithSchedulesDto getRouteWithSchedules(String flightNumber){

        RouteEntity routeEntity = getRouteEntity(flightNumber);

        List<DayOfWeek> weekDays = new ArrayList<>();
        for(RouteDayEntity routeDay : routeEntity.getRouteDays()){
            weekDays.add(routeDay.getWeekDay());
        }

        List<LocalTime> schedules = new ArrayList<>();
        for(RouteScheduleEntity schedule : routeEntity.getRouteSchedules()){
            schedules.add(schedule.getDepartureLocalTime());
        }

        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), weekDays, schedules);

    }


}
