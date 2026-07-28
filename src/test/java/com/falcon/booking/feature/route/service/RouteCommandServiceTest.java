package com.falcon.booking.feature.route.service;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.common.enums.RouteStatus;
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
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.RouteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RouteCommandServiceTest {

    @Mock
    private RouteQueryService routeQueryService;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteMapper routeMapper;
    @Mock
    private AirplaneTypeService airplaneTypeService;
    @Mock
    private AirportService airportService;

    @InjectMocks
    private RouteCommandService routeCommandService;

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

    @DisplayName("Should add route when data is valid")
    @Test
    void shouldAddRoute_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        RouteEntity routeToSave = createRouteEntity("AV1234");
        routeToSave.setStatus(null);
        RouteEntity savedRoute = createRouteEntity("AV1234");
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.DRAFT, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        AirplaneTypeEntity airplaneType = createAirplaneType(AirplaneTypeStatus.ACTIVE);
        AirportEntity origin = createAirport(1L, "BOG");
        AirportEntity destination = createAirport(2L, "MDE");

        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(false);
        given(airplaneTypeService.getAirplaneTypeEntity(1L)).willReturn(airplaneType);
        given(airportService.getAirportEntityByIataCode("BOG")).willReturn(origin);
        given(airportService.getAirportEntityByIataCode("MDE")).willReturn(destination);
        given(routeMapper.toEntity(createDto)).willReturn(routeToSave);
        given(routeRepository.save(routeToSave)).willReturn(savedRoute);
        given(routeMapper.toResponseDto(savedRoute)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.addRoute(createDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(routeToSave.isDraft()).isTrue();
    }

    @DisplayName("Should throw exception when route already exists")
    @Test
    void shouldThrowException_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(true);

        assertThrows(RouteAlreadyExistsException.class, () -> routeCommandService.addRoute(createDto));
        verify(routeRepository, never()).save(any());
    }

    @DisplayName("Should throw exception when route has same origin and destination")
    @Test
    void shouldThrowExceptionSameOriginAndDestination_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "BOG", 1L, 60, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        given(routeRepository.existsByFlightNumber(anyString())).willReturn(false);

        assertThrows(RouteSameOriginAndDestinationException.class,
                () -> routeCommandService.addRoute(createDto));
    }

    @DisplayName("Should throw exception when airplane type is not active")
    @Test
    void shouldThrowExceptionAirplaneNotActive_addRoute() {
        CreateRouteDto createDto = new CreateRouteDto("AV1234", "BOG", "MDE", 1L, 60, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        AirplaneTypeEntity airplaneType = createAirplaneType(AirplaneTypeStatus.INACTIVE);

        given(routeRepository.existsByFlightNumber("AV1234")).willReturn(false);
        given(airplaneTypeService.getAirplaneTypeEntity(1L)).willReturn(airplaneType);

        assertThrows(RouteAirplaneTypeIsNotActiveException.class, () -> routeCommandService.addRoute(createDto));
    }

    @DisplayName("Should update route when data is valid")
    @Test
    void shouldUpdateRoute_updateRoute() {
        RouteEntity route = createRouteEntity("AV1234");
        UpdateRouteDto updateDto = new UpdateRouteDto(null, null, null, 90, null, null);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 90, RouteStatus.DRAFT, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));
        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.updateRoute("AV1234", updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.getDurationMinutes()).isEqualTo(90);
    }

    @DisplayName("Should deactivate route")
    @Test
    void shouldDeactivateRoute_deactivateRoute() {
        RouteEntity route = createRouteEntity("AV1234");
        route.setStatus(RouteStatus.ACTIVE);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.INACTIVE, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.deactivateRoute("AV1234");

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.isInactive()).isTrue();
    }

    @DisplayName("Should activate route")
    @Test
    void shouldActivateRoute_activateRoute() {
        RouteEntity route = createRouteEntity("AV1234");
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.ACTIVE, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.activateRoute("AV1234");

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.isActive()).isTrue();
    }

    @DisplayName("Should update route with optional fields")
    @Test
    void shouldUpdateRoute_updateRoute_withOptionalFields() {
        RouteEntity route = createRouteEntity("AV1234");
        route.setStatus(RouteStatus.DRAFT);
        AirportEntity newOrigin = new AirportEntity();
        newOrigin.setIataCode("CTG");
        newOrigin.setTimezone("America/Bogota");
        AirportEntity newDestination = new AirportEntity();
        newDestination.setIataCode("BAQ");
        newDestination.setTimezone("America/Bogota");
        AirplaneTypeEntity newAirplaneType = createAirplaneType(AirplaneTypeStatus.ACTIVE);
        newAirplaneType.setId(2L);

        UpdateRouteDto updateDto = new UpdateRouteDto("CTG", "BAQ", 2L, 90, null, null);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 90, RouteStatus.DRAFT, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(airportService.getAirportEntityByIataCode("CTG")).willReturn(newOrigin);
        given(airportService.getAirportEntityByIataCode("BAQ")).willReturn(newDestination);
        given(airplaneTypeService.getAirplaneTypeEntity(2L)).willReturn(newAirplaneType);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.updateRoute("AV1234", updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.getDurationMinutes()).isEqualTo(90);
        assertThat(route.getAirportOrigin().getIataCode()).isEqualTo("CTG");
        assertThat(route.getAirportDestination().getIataCode()).isEqualTo("BAQ");
        assertThat(route.getDefaultAirplaneType().getId()).isEqualTo(2L);
    }

    @DisplayName("Should throw exception when updating non-draft route with destination changes")
    @Test
    void shouldThrowException_updateRoute_nonDraftWithDestinationChanges() {
        RouteEntity route = createRouteEntity("AV1234");
        route.setStatus(RouteStatus.ACTIVE);
        UpdateRouteDto updateDto = new UpdateRouteDto("CTG", null, null, null, null, null);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteDraftInvalidUpdateException.class, () ->
                routeCommandService.updateRoute("AV1234", updateDto));
    }

    @DisplayName("Should throw exception when updating route with same origin and destination")
    @Test
    void shouldThrowException_updateRoute_sameOriginAndDestination() {
        RouteEntity route = createRouteEntity("AV1234");
        route.getAirportOrigin().setIataCode("BOG");
        route.getAirportDestination().setIataCode("BOG");
        UpdateRouteDto updateDto = new UpdateRouteDto(null, null, null, 90, null, null);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteSameOriginAndDestinationException.class, () ->
                routeCommandService.updateRoute("AV1234", updateDto));
    }

    @DisplayName("Should throw exception when updating non-draft route with destination change")
    @Test
    void shouldThrowException_updateRoute_nonDraftWithOriginChange_shouldThrow() {
        RouteEntity route = createRouteEntity("AV1234");
        route.setStatus(RouteStatus.ACTIVE);
        UpdateRouteDto updateDto = new UpdateRouteDto(null, "BAQ", null, null, null, null);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteDraftInvalidUpdateException.class, () ->
                routeCommandService.updateRoute("AV1234", updateDto));
    }

    @DisplayName("Should update duration and base prices")
    @Test
    void shouldUpdateRoute_durationAndPrices() {
        RouteEntity route = createRouteEntity("AV1234");
        UpdateRouteDto updateDto = new UpdateRouteDto(null, null, null, 90, BigDecimal.valueOf(150), BigDecimal.valueOf(300));
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 90, RouteStatus.DRAFT, BigDecimal.valueOf(150), BigDecimal.valueOf(300));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.updateRoute("AV1234", updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.getDurationMinutes()).isEqualTo(90);
        assertThat(route.getBasePriceEconomy()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(route.getBasePriceFirstClass()).isEqualByComparingTo(BigDecimal.valueOf(300));
    }

    @DisplayName("Should not update fields when update dto has null values")
    @Test
    void shouldNotUpdateFields_whenDtoHasNulls() {
        RouteEntity route = createRouteEntity("AV1234");
        UpdateRouteDto updateDto = new UpdateRouteDto(null, null, null, null, null, null);
        ResponseRouteDto responseDto = new ResponseRouteDto("AV1234", null, null, null, 60, RouteStatus.DRAFT, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(routeMapper.toResponseDto(route)).willReturn(responseDto);

        ResponseRouteDto result = routeCommandService.updateRoute("AV1234", updateDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(route.getDurationMinutes()).isEqualTo(60);
    }
}
