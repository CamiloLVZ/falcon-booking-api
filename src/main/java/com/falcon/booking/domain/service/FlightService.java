package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.Flight.FlightDoesNotExistException;
import com.falcon.booking.domain.mapper.AirplaneTypeMapper;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGeneratedDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final FlightMapper flightMapper;

    @Autowired
    public FlightService(FlightRepository flightRepository, RouteService routeService, FlightMapper flightMapper, AirplaneTypeMapper airplaneTypeMapper) {
        this.flightRepository = flightRepository;
        this.routeService = routeService;
        this.flightMapper = flightMapper;
    }

    @Transactional
    public ResponseFlightDto addFlight(CreateFlightDto createFlightDto) {

        RouteEntity route = routeService.getRouteEntity(createFlightDto.routeFlightNumber());

        FlightEntity entityToSave = new FlightEntity(route, route.getDefaultAirplaneType(),
                                                createFlightDto.departureDateTime(), FlightStatus.SCHEDULED);

       FlightEntity entitySaved = flightRepository.save(entityToSave);

       return flightMapper.toDto(entitySaved);
    }

    public ResponseFlightDto getFlightById(Long id) {
        FlightEntity flightEntity = flightRepository.findById(id)
                .orElseThrow( () -> new FlightDoesNotExistException(id));

        return flightMapper.toDto(flightEntity);
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
