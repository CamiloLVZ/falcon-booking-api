package com.falcon.booking.feature.route.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.route.dto.AddRouteScheduleRequestDto;
import com.falcon.booking.feature.route.dto.RouteWithSchedulesDto;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.RouteDayRepository;
import com.falcon.booking.persistence.repository.RouteRepository;
import com.falcon.booking.persistence.repository.RouteScheduleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RouteSchedulesServiceTest {

    @Mock
    private RouteDayRepository routeDayRepository;
    @Mock
    private RouteScheduleRepository routeScheduleRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteQueryService routeQueryService;

    @InjectMocks
    private RouteSchedulesService routeSchedulesService;

    private AirplaneTypeEntity createAirplaneType(AirplaneTypeStatus status) {
        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.setId(1L);
        airplaneType.setProducer("Airbus");
        airplaneType.setModel("A320");
        airplaneType.configureSeats(108, 12, "ABCDEF");
        airplaneType.setStatus(status);
        return airplaneType;
    }

    private AirportEntity createAirport(Long id, String iataCode) {
        AirportEntity airport = new AirportEntity();
        airport.setId(id);
        airport.setIataCode(iataCode);
        airport.setTimezone("America/Bogota");
        return airport;
    }

    private RouteEntity createRouteEntity(String flightNumber) {
        RouteEntity route = new RouteEntity();
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(createAirport(1L, "BOG"));
        route.setAirportDestination(createAirport(2L, "MDE"));
        route.setDefaultAirplaneType(createAirplaneType(AirplaneTypeStatus.ACTIVE));
        route.setDurationMinutes(60);
        route.setStatus(RouteStatus.DRAFT);
        route.setRouteDays(new HashSet<>(List.of(new RouteDayEntity(route, DayOfWeek.MONDAY))));
        route.setRouteSchedules(new HashSet<>(List.of(new RouteScheduleEntity(route, LocalTime.of(8, 0)))));
        return route;
    }

    @DisplayName("Should set route operating schedules")
    @Test
    void shouldSetRouteOperatingSchedules_setRouteOperatingSchedules() {
        RouteEntity route = createRouteEntity("AV1234");
        AddRouteScheduleRequestDto requestDto = new AddRouteScheduleRequestDto(
                Set.of(LocalTime.of(10, 0), LocalTime.of(15, 0)),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
        );

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        RouteWithSchedulesDto result = routeSchedulesService.setRouteOperatingSchedules("AV1234", requestDto);

        verify(routeDayRepository).deleteAllByRoute(route);
        verify(routeScheduleRepository).deleteAllByRoute(route);
        assertThat(result.flightNumber()).isEqualTo("AV1234");
        assertThat(result.daysOfWeek()).hasSize(2);
        assertThat(result.schedules()).hasSize(2);
    }

    @DisplayName("Should return route with schedules")
    @Test
    void shouldReturnRouteWithSchedules_getRouteWithSchedules() {
        RouteEntity route = createRouteEntity("AV1234");
        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        RouteWithSchedulesDto result = routeSchedulesService.getRouteWithSchedules("AV1234");

        assertThat(result.flightNumber()).isEqualTo("AV1234");
        assertThat(result.daysOfWeek()).contains(DayOfWeek.MONDAY);
        assertThat(result.schedules()).contains(LocalTime.of(8, 0));
    }
}
