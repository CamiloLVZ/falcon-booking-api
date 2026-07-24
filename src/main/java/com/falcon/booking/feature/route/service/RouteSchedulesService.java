package com.falcon.booking.feature.route.service;

import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.RouteDayRepository;
import com.falcon.booking.persistence.repository.RouteScheduleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Slf4j
@Service
public class RouteSchedulesService {

    private final RouteQueryService routeQueryService;
    private final RouteDayRepository routeDayRepository;
    private final RouteScheduleRepository routeScheduleRepository;

    @Autowired
    public RouteSchedulesService(RouteQueryService routeQueryService, RouteDayRepository routeDayRepository, RouteScheduleRepository routeScheduleRepository) {
        this.routeQueryService = routeQueryService;
        this.routeDayRepository = routeDayRepository;
        this.routeScheduleRepository = routeScheduleRepository;
    }

    @Transactional
    public RouteWithSchedulesDto setRouteOperatingSchedules(String flightNumber, AddRouteScheduleRequestDto requestDto) {
        RouteEntity routeEntity = routeQueryService.getRouteEntity(flightNumber);
        if (requestDto.daysOfWeek() != null) {
            setRouteDays(routeEntity, requestDto.daysOfWeek());
        }
        if(requestDto.schedules() != null) {
            setRouteSchedules(routeEntity, requestDto.schedules());
        }
        log.info("Route {} set operating schedules: {}, {} ", routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());
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

        RouteEntity routeEntity = routeQueryService.getRouteEntity(flightNumber);
        return new RouteWithSchedulesDto(routeEntity.getFlightNumber(), routeEntity.getOperatingDays(), routeEntity.getOperatingSchedules());

    }
}
