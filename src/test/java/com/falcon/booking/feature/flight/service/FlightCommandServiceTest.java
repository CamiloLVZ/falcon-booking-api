package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.feature.flight.dto.CreateFlightDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightAlreadyExistsException;
import com.falcon.booking.feature.flight.exception.FlightCanNotBeRescheduledException;
import com.falcon.booking.feature.flight.exception.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlightCommandServiceTest {

    @Mock private FlightRepository flightRepository;
    @Mock private FlightQueryService flightQueryService;
    @Mock private RouteQueryService routeQueryService;
    @Mock private AirplaneTypeService airplaneTypeService;
    @Mock private FlightMapper flightMapper;

    @InjectMocks
    private FlightCommandService flightCommandService;

    private AirportEntity createAirport(String timezone) {
        AirportEntity airport = new AirportEntity();
        airport.setId(1L);
        airport.setIataCode("BOG");
        airport.setTimezone(timezone);
        return airport;
    }

    private AirplaneTypeEntity createAirplaneType(Long id) {
        AirplaneTypeEntity type = new AirplaneTypeEntity();
        type.setId(id);
        type.setProducer("Airbus");
        type.setModel("A320");
        type.configureSeats(150, 12, "ABCDEF");
        type.setStatus(com.falcon.booking.common.enums.AirplaneTypeStatus.ACTIVE);
        return type;
    }

    private RouteEntity createRoute(String flightNumber, String timezone, boolean active) {
        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(createAirport(timezone));
        route.setAirportDestination(createAirport(timezone));
        route.setDefaultAirplaneType(createAirplaneType(1L));
        route.setDurationMinutes(120);
        route.setStatus(active ? RouteStatus.ACTIVE : RouteStatus.INACTIVE);
        return route;
    }

    private FlightEntity createFlight(Long id, RouteEntity route, OffsetDateTime departureDateTime, FlightStatus status) {
        FlightEntity flight = new FlightEntity(route, route.getDefaultAirplaneType(), departureDateTime, status);
        flight.setId(id);
        return flight;
    }

    @DisplayName("Should add flight successfully")
    @Test
    void shouldAddFlight_success() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        CreateFlightDto request = new CreateFlightDto("AV1234", LocalDateTime.of(2026, 8, 1, 14, 0));
        OffsetDateTime expectedDeparture = request.departureDateTime().atZone(ZoneId.of("UTC")).toOffsetDateTime();
        FlightEntity savedEntity = createFlight(1L, route, expectedDeparture, FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedDeparture, expectedDeparture.toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, expectedDeparture)).willReturn(false);
        given(flightRepository.save(any(FlightEntity.class))).willReturn(savedEntity);
        given(flightMapper.toDto(savedEntity)).willReturn(dto);

        ResponseFlightDto result = flightCommandService.addFlight(request);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw FlightAlreadyExistsException when adding duplicate flight")
    @Test
    void shouldThrowWhenAddFlightAlreadyExists() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        CreateFlightDto request = new CreateFlightDto("AV1234", LocalDateTime.of(2026, 8, 1, 14, 0));
        OffsetDateTime expectedDeparture = request.departureDateTime().atZone(ZoneId.of("UTC")).toOffsetDateTime();

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, expectedDeparture)).willReturn(true);

        assertThrows(FlightAlreadyExistsException.class, () -> flightCommandService.addFlight(request));
    }

    @DisplayName("Should cancel an existing flight")
    @Test
    void shouldCancelFlight() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.CANCELED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(flightQueryService.getFlightEntity(1L)).willReturn(flight);
        given(flightRepository.save(flight)).willReturn(flight);
        given(flightMapper.toDto(flight)).willReturn(dto);

        ResponseFlightDto result = flightCommandService.cancelFlight(1L);
        assertThat(result).isEqualTo(dto);
        assertThat(flight.isCanceled()).isTrue();
    }

    @DisplayName("Should reschedule scheduled flight")
    @Test
    void shouldRescheduleFlight_success() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime oldDeparture = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        FlightEntity oldFlight = createFlight(1L, route, oldDeparture, FlightStatus.SCHEDULED);
        LocalDateTime nextDeparture = LocalDateTime.of(2026, 8, 5, 16, 0);
        OffsetDateTime newDeparture = nextDeparture.atZone(ZoneId.of("UTC")).toOffsetDateTime();
        FlightEntity savedFlight = createFlight(2L, route, newDeparture, FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(2L, "AV1234", "BOG", "BOG", newDeparture, newDeparture.toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(flightQueryService.getFlightEntity(1L)).willReturn(oldFlight);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, newDeparture)).willReturn(false);
        given(flightRepository.save(any(FlightEntity.class))).willReturn(savedFlight);
        given(flightMapper.toDto(savedFlight)).willReturn(dto);

        ResponseFlightDto result = flightCommandService.rescheduleFLight(1L, nextDeparture);
        assertThat(result).isEqualTo(dto);
        assertThat(oldFlight.isCanceled()).isTrue();
    }

    @DisplayName("Should throw FlightCanNotBeRescheduledException when status invalid")
    @Test
    void shouldThrowWhenRescheduleFlightInvalidStatus() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.BOARDING);
        given(flightQueryService.getFlightEntity(1L)).willReturn(flight);

        assertThrows(FlightCanNotBeRescheduledException.class, () -> flightCommandService.rescheduleFLight(1L, LocalDateTime.of(2026, 8, 1, 16, 0)));
    }

    @DisplayName("Should throw FlightAlreadyExistsException when rescheduling to duplicated departure")
    @Test
    void shouldThrowWhenRescheduleFlightAlreadyExists() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        LocalDateTime nextDeparture = LocalDateTime.of(2026, 8, 5, 16, 0);
        OffsetDateTime newDeparture = nextDeparture.atZone(ZoneId.of("UTC")).toOffsetDateTime();

        given(flightQueryService.getFlightEntity(1L)).willReturn(flight);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, newDeparture)).willReturn(true);

        assertThrows(FlightAlreadyExistsException.class, () -> flightCommandService.rescheduleFLight(1L, nextDeparture));
    }

    @DisplayName("Should change airplane type for scheduled flight")
    @Test
    void shouldChangeAirplaneType_success() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        AirplaneTypeEntity newType = createAirplaneType(2L);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(flightQueryService.getFlightEntity(1L)).willReturn(flight);
        given(airplaneTypeService.getAirplaneTypeEntity(2L)).willReturn(newType);
        given(flightRepository.save(flight)).willReturn(flight);
        given(flightMapper.toDto(flight)).willReturn(dto);

        ResponseFlightDto result = flightCommandService.changeAirplaneType(1L, 2L);
        assertThat(result).isEqualTo(dto);
        assertThat(flight.getAirplaneType()).isSameAs(newType);
    }

    @DisplayName("Should throw FlightCanNotChangeAirplaneTypeException when flight is not scheduled")
    @Test
    void shouldThrowWhenChangeAirplaneTypeInvalidStatus() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.BOARDING);
        given(flightQueryService.getFlightEntity(1L)).willReturn(flight);

        assertThrows(FlightCanNotChangeAirplaneTypeException.class, () -> flightCommandService.changeAirplaneType(1L, 2L));
        verify(airplaneTypeService, never()).getAirplaneTypeEntity(any());
    }
}
