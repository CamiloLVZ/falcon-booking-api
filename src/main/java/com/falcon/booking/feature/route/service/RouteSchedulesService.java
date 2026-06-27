package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.RouteDayRepository;
import com.falcon.booking.persistence.repository.RouteScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Service
public class RouteSchedulesService {

    private static final Logger logger = LoggerFactory.getLogger(RouteSchedulesService.class);

    private final RouteService routeService;
    private final RouteDayRepository routeDayRepository;
    private final RouteScheduleRepository routeScheduleRepository;

    @Autowired
    public RouteSchedulesService(RouteService routeService, RouteDayRepository routeDayRepository, RouteScheduleRepository routeScheduleRepository) {
        this.routeService = routeService;
        this.routeDayRepository = routeDayRepository;
        this.routeScheduleRepository = routeScheduleRepository;
    }

    @Transactional
    public RouteWithSchedulesDto setRouteOperatingSchedules(String flightNumber, AddRouteScheduleRequestDto requestDto) {
        RouteEntity routeEntity = routeService.getRouteEntity(flightNumber);
        if (requestDto.daysOfWeek() != null) {
            setRouteDays(routeEntity, requestDto.daysOfWeek());
        }
        if(requestDto.schedules() != null) {
            setRouteSchedules(routeEntity, requestDto.schedules());
        }
        logger.info("Route {} set operating schedules: {}, {} ", routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());
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

        RouteEntity routeEntity = routeService.getRouteEntity(flightNumber);
        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());

    }
}
