package com.falcon.booking.feature.flightGeneration.service;

import com.falcon.booking.common.enums.FlightGenerationStatus;
import com.falcon.booking.common.enums.FlightGenerationType;
import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.airplaneType.service.AirplaneTypeService;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.flightGeneration.dto.ResponseFlightsGenerationDto;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationAlreadyRunningException;
import com.falcon.booking.feature.flightGeneration.mapper.FlightGenerationMapper;
import com.falcon.booking.feature.route.exception.RouteNotActiveException;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import com.falcon.booking.persistence.repository.FlightRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FlightGenerationServiceTest {

    @Mock
    private RouteQueryService routeQueryService;

    @Mock
    private AsyncFlightGenerationService asyncFlightGenerationService;

    @Mock
    private FlightGenerationRepository flightGenerationRepository;

    @Mock
    private FlightGenerationMapper flightGenerationMapper;

    @InjectMocks
    private FlightGenerationService flightGenerationService;

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

    @DisplayName("Should start global flight generation")
    @Test
    void shouldStartGlobalFlightGeneration() {
        FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
        generation.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, generation.getStartedAt(), null, null, "/flight-generations/1");

        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);
        given(flightGenerationMapper.toDto(generation)).willReturn(dto);

        ResponseFlightsGenerationDto result = flightGenerationService.startGlobalFlightGeneration();
        verify(asyncFlightGenerationService).executeGeneration(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_GlobalGeneration() {
        var constraintException = new ConstraintViolationException("duplicate", null, FlightGenerationService.SINGLE_RUNNING_GENERATION_CONSTRAINT);
        var dataException = new DataIntegrityViolationException("duplicate", constraintException);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willThrow(dataException);

        assertThrows(
                FlightGenerationAlreadyRunningException.class,
                flightGenerationService::startGlobalFlightGeneration
        );
    }

    @DisplayName("Should start route flight generation")
    @Test
    void shouldStartRouteFlightGeneration() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightGenerationEntity generation = FlightGenerationEntity.startRouteGeneration(route.getId());
        generation.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.ROUTE, route.getId(), null, generation.getStartedAt(), null, null, "/flight-generations/1");

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);
        given(flightGenerationMapper.toDto(generation)).willReturn(dto);

        ResponseFlightsGenerationDto result = flightGenerationService.startRouteFlightGeneration("AV1234");

        verify(asyncFlightGenerationService).executeGeneration(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw RouteNotActiveException when route generation is requested for inactive route")
    @Test
    void shouldThrowWhenRouteInactive_RouteGeneration() {
        RouteEntity route = createRoute("AV1234", "UTC", false);
        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteNotActiveException.class, () -> flightGenerationService.startRouteFlightGeneration("AV1234"));
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_RouteGeneration() {

        RouteEntity route = createRoute("AV1234", "UTC", true);
        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        var constraintException = new ConstraintViolationException("duplicate", null, FlightGenerationService.SINGLE_RUNNING_GENERATION_CONSTRAINT);
        var dataException = new DataIntegrityViolationException("duplicate", constraintException);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willThrow(dataException);

        assertThrows(
                FlightGenerationAlreadyRunningException.class,
                () -> flightGenerationService.startRouteFlightGeneration("AV1234")
        );
    }


    @DisplayName("Should start daily flight generation")
    @Test
    void shouldStartDailyFlightGeneration() {
        FlightGenerationEntity generation = FlightGenerationEntity.startDailyGeneration(LocalDate.now());
        generation.setId(1L);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);

        flightGenerationService.startDailyFlightGeneration(LocalDate.now());

        verify(flightGenerationRepository).save(any(FlightGenerationEntity.class));
        verify(asyncFlightGenerationService).executeGeneration(any(Long.class));
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_DailyGeneration() {
        var constraintException = new ConstraintViolationException("duplicate", null, FlightGenerationService.SINGLE_RUNNING_GENERATION_CONSTRAINT);
        var dataException = new DataIntegrityViolationException("duplicate", constraintException);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willThrow(dataException);

        assertThrows(
                FlightGenerationAlreadyRunningException.class,
                () -> flightGenerationService.startDailyFlightGeneration(LocalDate.now())
        );
    }

    @DisplayName("Should return all flight generations when no filters applied")
    @Test
    void shouldReturnAllFlightGenerations_noFilters() {
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, entity.getStartedAt(), null, null, "/flight-generations/1");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startedAt").descending());
        Page<FlightGenerationEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        given(flightGenerationRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);
        given(flightGenerationMapper.toDto(entity)).willReturn(dto);

        Page<ResponseFlightsGenerationDto> result = flightGenerationService.getAllFlightGenerations(null, null, null, 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @DisplayName("Should filter flight generations by route flight number")
    @Test
    void shouldFilterFlightGenerationsByRouteFlightNumber() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        route.setId(5L);
        FlightGenerationEntity entity = FlightGenerationEntity.startRouteGeneration(5L);
        entity.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.ROUTE, 5L, null, entity.getStartedAt(), null, null, "/flight-generations/1");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startedAt").descending());
        Page<FlightGenerationEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        given(flightGenerationRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);
        given(flightGenerationMapper.toDto(entity)).willReturn(dto);

        Page<ResponseFlightsGenerationDto> result = flightGenerationService.getAllFlightGenerations(null, null, "AV1234", 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
        verify(routeQueryService).getRouteEntity("AV1234");
    }

    @DisplayName("Should ignore blank route flight number filter")
    @Test
    void shouldIgnoreBlankRouteFlightNumber() {
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, entity.getStartedAt(), null, null, "/flight-generations/1");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startedAt").descending());
        Page<FlightGenerationEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        given(flightGenerationRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);
        given(flightGenerationMapper.toDto(entity)).willReturn(dto);

        Page<ResponseFlightsGenerationDto> result = flightGenerationService.getAllFlightGenerations(null, null, "   ", 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @DisplayName("Should filter flight generations by type and status")
    @Test
    void shouldFilterFlightGenerationsByTypeAndStatus() {
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(1L);
        entity.setStatus(FlightGenerationStatus.COMPLETED);
        entity.setFinishedAt(Instant.now());
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.COMPLETED, FlightGenerationType.GLOBAL, null, null, entity.getStartedAt(), entity.getFinishedAt(), null, "/flight-generations/1");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startedAt").descending());
        Page<FlightGenerationEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

        given(flightGenerationRepository.findAll(any(Specification.class), eq(pageable))).willReturn(page);
        given(flightGenerationMapper.toDto(entity)).willReturn(dto);

        Page<ResponseFlightsGenerationDto> result = flightGenerationService.getAllFlightGenerations(FlightGenerationType.GLOBAL, FlightGenerationStatus.COMPLETED, null, 0, 10);

        assertThat(result.getContent()).containsExactly(dto);
    }

    @DisplayName("Should throw DataIntegrityViolationException when the error is not generation already running")
    @Test
    void shouldNotTranslateOtherConstraintsException() {

        var constraintException = new ConstraintViolationException("check violation", null, "chk_route_required_for_route_flight_generation");
        var dataException = new DataIntegrityViolationException("error", constraintException);
        given(flightGenerationRepository.save(any())).willThrow(dataException);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> flightGenerationService.startDailyFlightGeneration(LocalDate.now())
        );
    }

}
