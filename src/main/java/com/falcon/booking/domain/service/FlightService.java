package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.DateToBeforeDateFromException;
import com.falcon.booking.domain.exception.Flight.FlightAlreadyExistsException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotBeRescheduledException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.domain.exception.Flight.FlightDoesNotExistException;
import com.falcon.booking.domain.exception.Route.RouteNotActiveException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.specification.FlightSpecifications;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class FlightService {

    @Value("${app.generation.horizon-days}")
    int flightGenerationDaysHorizon;

    @Value("${app.flight.hours-before-boarding}")
    int hoursBeforeFlightStartsBoarding;

    private final FlightRepository flightRepository;
    private final RouteService routeService;
    private final AirplaneTypeService airplaneTypeService;
    private final FlightMapper flightMapper;

    @Autowired
    public FlightService(FlightRepository flightRepository, RouteService routeService, FlightMapper flightMapper, AirplaneTypeMapper airplaneTypeMapper, AirplaneTypeService airplaneTypeService) {
        this.flightRepository = flightRepository;
        this.routeService = routeService;
        this.flightMapper = flightMapper;
        this.airplaneTypeService = airplaneTypeService;
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
    public List<ResponseFlightsGeneratedDto> generateFlightsForAllRoutes() {
        List<ResponseFlightsGeneratedDto> dtoList = new ArrayList<>();
        List<RouteEntity> routeEntities = routeService.getAllRoutesByStatus(RouteStatus.ACTIVE);
        for(RouteEntity routeEntity : routeEntities) {
            dtoList.add(generateFlights(routeEntity));
        }
        return dtoList;
    }

    @Transactional
    public ResponseFlightsGeneratedDto generateFlightsForRoute(String flightNumber) {

        RouteEntity route = routeService.getRouteEntity(flightNumber);
        return generateFlights(route);

    }

    @Transactional
    public ResponseFlightsGeneratedDto generateFlights(RouteEntity route) {

        ZoneId timeZoneId = ZoneId.of(route.getAirportOrigin().getTimezone());
        Set<DayOfWeek> routeDays = route.getOperatingDays();
        Set<LocalTime> routeSchedules = route.getOperatingSchedules();

        LocalDate today = LocalDate.now(timeZoneId);
        LocalTime localTime = LocalTime.now(timeZoneId);

        if(routeDays.contains(today.getDayOfWeek())){
            for(LocalTime departureTime: routeSchedules){
                LocalTime localBoardingTime = departureTime.minusHours(hoursBeforeFlightStartsBoarding);
                if(localTime.isBefore(localBoardingTime)){
                    LocalDateTime departureDateTime = LocalDateTime.of(today, departureTime);
                    OffsetDateTime offsetDepartureDateTime = departureDateTime.atZone(timeZoneId).toOffsetDateTime();
                    if(flightRepository.existsByRouteAndDepartureDateTime(route, offsetDepartureDateTime))
                        continue;
                    flightRepository.save(new FlightEntity(route, route.getDefaultAirplaneType(), offsetDepartureDateTime, FlightStatus.SCHEDULED));
                }
            }
        }
        LocalDate horizonDate = today.plusDays(flightGenerationDaysHorizon);
        LocalDate dateIterator = today.plusDays(1);
        int flightCounter = 0;
        while(dateIterator.isBefore(horizonDate)) {
                if(routeDays.contains(dateIterator.getDayOfWeek())) {

                        List<FlightEntity> flightEntities = generateFlightsBySchedules(routeSchedules, dateIterator, timeZoneId, route);
                        flightRepository.saveAll(flightEntities);
                        flightCounter += flightEntities.size();

                }
            dateIterator = dateIterator.plusDays(1);
        }
        return new ResponseFlightsGeneratedDto(route.getFlightNumber(), flightCounter,
                LocalDate.now(), LocalDate.now().plusDays(flightGenerationDaysHorizon) );
    }

    private List<FlightEntity> generateFlightsBySchedules(Set<LocalTime> routeSchedules, LocalDate date, ZoneId timeZoneId, RouteEntity route) {
        List<FlightEntity> flightEntities = new ArrayList<>();
        for(LocalTime time : routeSchedules) {
            LocalDateTime datetime = LocalDateTime.of(date, time);
            OffsetDateTime departureDateTime = datetime.atZone(timeZoneId).toOffsetDateTime();

            if(flightRepository.existsByRouteAndDepartureDateTime(route, departureDateTime))
                continue;

            flightEntities.add( new FlightEntity(route, route.getDefaultAirplaneType(),
                    departureDateTime, FlightStatus.SCHEDULED));

        }
        return flightEntities;
    }
}
