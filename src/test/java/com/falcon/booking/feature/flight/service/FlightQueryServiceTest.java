package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightNotFoundException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.route.exception.RouteNotActiveException;
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
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FlightQueryServiceTest {

    @Mock private FlightRepository flightRepository;
    @Mock private RouteQueryService routeQueryService;
    @Mock private AirportService airportService;
    @Mock private FlightMapper flightMapper;

    @InjectMocks
    private FlightQueryService flightQueryService;

    private AirportEntity createAirport(String timezone) {
        AirportEntity airport = new AirportEntity();
        airport.setId(1L);
        airport.setIataCode("BOG");
        airport.setName("Bogota Airport");
        airport.setCity("Bogota");
        airport.setTimezone(timezone);
        return airport;
    }

    private AirplaneTypeEntity createAirplaneType(Long id) {
        AirplaneTypeEntity type = new AirplaneTypeEntity();
        type.setId(id);
        type.setProducer("Airbus");
        type.setModel("A320");
        type.setEconomySeats(150);
        type.setFirstClassSeats(10);
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

    @DisplayName("Should return FlightEntity when exists")
    @Test
    void shouldReturnFlightEntity_whenExists() {
        FlightEntity entity = createFlight(1L, createRoute("AV1234", "UTC", true), OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        given(flightRepository.findById(1L)).willReturn(Optional.of(entity));

        FlightEntity result = flightQueryService.getFlightEntity(1L);

        assertThat(result).isSameAs(entity);
    }

    @DisplayName("Should throw FlightNotFoundException when flight does not exist")
    @Test
    void shouldThrowException_getFlightEntity_whenNotFound() {
        given(flightRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> flightQueryService.getFlightEntity(1L));
    }

    @DisplayName("Should return flight dto by id")
    @Test
    void shouldReturnDto_getFlightById() {
        FlightEntity entity = createFlight(1L, createRoute("AV1234", "UTC", true), OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", entity.getDepartureDateTime(), entity.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED);

        given(flightRepository.findById(1L)).willReturn(Optional.of(entity));
        given(flightMapper.toDto(entity)).willReturn(dto);

        ResponseFlightDto result = flightQueryService.getFlightById(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should return flights filtered by search criteria")
    @Test
    void shouldReturnFlights_whenGetAllFlights() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity expectedFlight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedFlight.getDepartureDateTime(), expectedFlight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        Page<FlightEntity> flightPage = new PageImpl<>(List.of(expectedFlight), PageRequest.of(0, 10, Sort.by("departureDateTime").ascending()), 1);
        given(flightRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by("departureDateTime").ascending())))).willReturn(flightPage);
        given(flightMapper.toDto(expectedFlight)).willReturn(dto);

        Page<ResponseFlightDto> result = flightQueryService.getAllFlights("AV1234", FlightStatus.SCHEDULED, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), 0, 10);
        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("Should throw RouteNotActiveException when route is inactive")
    @Test
    void shouldThrowWhenGetAllFlightsByRouteAndDates_routeInactive() {
        RouteEntity route = createRoute("AV1234", "UTC", false);
        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteNotActiveException.class,
                () -> flightQueryService.getAllFlightsByRouteAndDate("AV1234", LocalDate.of(2026, 8, 1), 0, 10));
    }

    @DisplayName("Should return all flights by route and date range")
    @Test
    void shouldReturnFlightsByRouteAndDates() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("departureDateTime").ascending());
        Page<FlightEntity> flightPage = new PageImpl<>(List.of(flight), pageable, 1);
        given(flightRepository.findAllByRouteAndDepartureDateTimeBetween(eq(route), any(OffsetDateTime.class), any(OffsetDateTime.class), eq(pageable))).willReturn(flightPage);
        given(flightMapper.toDto(flight)).willReturn(dto);

        Page<ResponseFlightDto> result = flightQueryService.getAllFlightsByRouteAndDate("AV1234", LocalDate.of(2026, 8, 1), 0, 10);
        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("Should return paginated flights ordered by departure date descending")
    @Test
    void shouldReturnFlightsPaginated_OrderedDesc() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity expectedFlight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedFlight.getDepartureDateTime(), expectedFlight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED);

        Page<FlightEntity> flightPage = new PageImpl<>(List.of(expectedFlight), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "departureDateTime")), 1);
        given(flightRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "departureDateTime"))))).willReturn(flightPage);
        given(flightMapper.toDto(expectedFlight)).willReturn(dto);

        Page<ResponseFlightDto> result = flightQueryService.getAllFlightsPaginated("AV1234", FlightStatus.SCHEDULED, 0, 10);
        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
