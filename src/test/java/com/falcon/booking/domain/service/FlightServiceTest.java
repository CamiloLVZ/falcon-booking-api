package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.DateToBeforeDateFromException;
import com.falcon.booking.domain.exception.Flight.FlightAlreadyExistsException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotBeRescheduledException;
import com.falcon.booking.domain.exception.Flight.FlightCanNotChangeAirplaneTypeException;
import com.falcon.booking.domain.exception.Flight.FlightNotFoundException;
import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationAlreadyRunningException;
import com.falcon.booking.domain.exception.FlightGeneration.FlightGenerationNotFoundException;
import com.falcon.booking.domain.exception.Route.RouteNotActiveException;
import com.falcon.booking.domain.mapper.FlightGenerationMapper;
import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.FlightGenerationEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightGenerationRepository;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.web.dto.flight.CreateFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightDto;
import com.falcon.booking.web.dto.flight.ResponseFlightsGenerationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private RouteService routeService;

    @Mock
    private AirplaneTypeService airplaneTypeService;

    @Mock
    private FlightMapper flightMapper;

    @Mock
    private AsyncFlightGenerationService asyncFlightGenerationService;

    @Mock
    private FlightGenerationRepository flightGenerationRepository;

    @Mock
    private FlightGenerationMapper flightGenerationMapper;

    @InjectMocks
    private FlightService flightService;

    @BeforeEach
    void setup() {
        flightService.checkInHoursBeforeToStart = 3;
        flightService.checkInHoursBeforeToClose = 1;
        flightService.boardingMinutesBeforeToStart = 30;
        flightService.boardingMinutesBeforeToClose = 10;
    }

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
        type.setStatus(null);
        return type;
    }

    private RouteEntity createRoute(String flightNumber, String timezone, boolean active) {
        RouteEntity route = new RouteEntity();
        route.setId(1L);
        route.setFlightNumber(flightNumber);
        route.setAirportOrigin(createAirport(timezone));
        route.setAirportDestination(createAirport(timezone));
        route.setDefaultAirplaneType(createAirplaneType(1L));
        route.setLengthMinutes(120);
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

        FlightEntity result = flightService.getFlightEntity(1L);

        assertThat(result).isSameAs(entity);
    }

    @DisplayName("Should throw FlightNotFoundException when flight does not exist")
    @Test
    void shouldThrowException_getFlightEntity_whenNotFound() {
        given(flightRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> flightService.getFlightEntity(1L));
    }

    @DisplayName("Should return flight dto by id")
    @Test
    void shouldReturnDto_getFlightById() {
        FlightEntity entity = createFlight(1L, createRoute("AV1234", "UTC", true), OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", entity.getDepartureDateTime(), entity.getDepartureDateTime().toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(flightRepository.findById(1L)).willReturn(Optional.of(entity));
        given(flightMapper.toDto(entity)).willReturn(dto);

        ResponseFlightDto result = flightService.getFlightById(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should return flight generation dto when exists")
    @Test
    void shouldReturnFlightGenerationDto_whenExists() {
        FlightGenerationEntity entity = FlightGenerationEntity.startGlobalGeneration();
        entity.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, entity.getStartedAt(), null, null, "/flight-generations/1");

        given(flightGenerationRepository.findById(1L)).willReturn(Optional.of(entity));
        given(flightGenerationMapper.toDto(entity)).willReturn(dto);

        ResponseFlightsGenerationDto result = flightService.getFlightGeneration(1L);

        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw FlightGenerationNotFoundException when flight generation does not exist")
    @Test
    void shouldThrowException_getFlightGeneration_whenNotFound() {
        given(flightGenerationRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(FlightGenerationNotFoundException.class, () -> flightService.getFlightGeneration(1L));
    }

    @DisplayName("Should return all flight generations")
    @Test
    void shouldReturnAllFlightGenerations() {
        FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
        generation.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, generation.getStartedAt(), null, null, "/flight-generations/1");

        given(flightGenerationRepository.findAll()).willReturn(List.of(generation));
        given(flightGenerationMapper.toDto(List.of(generation))).willReturn(List.of(dto));

        List<ResponseFlightsGenerationDto> result = flightService.getAllFlightGenerations();
        assertThat(result).containsExactly(dto);
    }

    @DisplayName("Should add flight successfully")
    @Test
    void shouldAddFlight_success() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        CreateFlightDto request = new CreateFlightDto("AV1234", LocalDateTime.of(2026, 8, 1, 14, 0));
        OffsetDateTime expectedDeparture = request.departureDateTime().atZone(ZoneId.of("UTC")).toOffsetDateTime();
        FlightEntity savedEntity = createFlight(1L, route, expectedDeparture, FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedDeparture, expectedDeparture.toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, expectedDeparture)).willReturn(false);
        given(flightRepository.save(any(FlightEntity.class))).willReturn(savedEntity);
        given(flightMapper.toDto(savedEntity)).willReturn(dto);

        ResponseFlightDto result = flightService.addFlight(request);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw FlightAlreadyExistsException when adding duplicate flight")
    @Test
    void shouldThrowWhenAddFlightAlreadyExists() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        CreateFlightDto request = new CreateFlightDto("AV1234", LocalDateTime.of(2026, 8, 1, 14, 0));
        OffsetDateTime expectedDeparture = request.departureDateTime().atZone(ZoneId.of("UTC")).toOffsetDateTime();

        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.existsByRouteAndDepartureDateTime(route, expectedDeparture)).willReturn(true);

        assertThrows(FlightAlreadyExistsException.class, () -> flightService.addFlight(request));
    }

    @DisplayName("Should return flights filtered by search criteria")
    @Test
    void shouldReturnFlights_whenGetAllFlights() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity expectedFlight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedFlight.getDepartureDateTime(), expectedFlight.getDepartureDateTime().toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.findAll(any(Specification.class))).willReturn(List.of(expectedFlight));
        given(flightMapper.toDto(List.of(expectedFlight))).willReturn(List.of(dto));

        List<ResponseFlightDto> result = flightService.getAllFlights("AV1234", FlightStatus.SCHEDULED, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
        assertThat(result).containsExactly(dto);
    }

    @DisplayName("Should cancel an existing flight")
    @Test
    void shouldCancelFlight() {
        FlightEntity flight = createFlight(1L, createRoute("AV1234", "UTC", true), OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), null, FlightStatus.CANCELED);

        given(flightRepository.findById(1L)).willReturn(Optional.of(flight));
        given(flightMapper.toDto(flight)).willReturn(dto);

        ResponseFlightDto result = flightService.cancelFlight(1L);
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
        ResponseFlightDto dto = new ResponseFlightDto(2L, "AV1234", "BOG", "BOG", newDeparture, newDeparture.toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(flightRepository.findById(1L)).willReturn(Optional.of(oldFlight));
        given(flightRepository.existsByRouteAndDepartureDateTime(route, newDeparture)).willReturn(false);
        given(flightRepository.save(any(FlightEntity.class))).willReturn(savedFlight);
        given(flightMapper.toDto(savedFlight)).willReturn(dto);

        ResponseFlightDto result = flightService.rescheduleFLight(1L, nextDeparture);

        assertThat(result).isEqualTo(dto);
        assertThat(oldFlight.isCanceled()).isTrue();
    }

    @DisplayName("Should throw FlightCanNotBeRescheduledException when status invalid")
    @Test
    void shouldThrowWhenRescheduleFlightInvalidStatus() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.BOARDING);

        given(flightRepository.findById(1L)).willReturn(Optional.of(flight));

        assertThrows(FlightCanNotBeRescheduledException.class, () -> flightService.rescheduleFLight(1L, LocalDateTime.of(2026, 8, 1, 16, 0)));
    }

    @DisplayName("Should throw FlightAlreadyExistsException when rescheduling to duplicated departure")
    @Test
    void shouldThrowWhenRescheduleFlightAlreadyExists() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        LocalDateTime nextDeparture = LocalDateTime.of(2026, 8, 5, 16, 0);
        OffsetDateTime newDeparture = nextDeparture.atZone(ZoneId.of("UTC")).toOffsetDateTime();

        given(flightRepository.findById(1L)).willReturn(Optional.of(flight));
        given(flightRepository.existsByRouteAndDepartureDateTime(route, newDeparture)).willReturn(true);

        assertThrows(FlightAlreadyExistsException.class, () -> flightService.rescheduleFLight(1L, nextDeparture));
    }

    @DisplayName("Should change airplane type for scheduled flight")
    @Test
    void shouldChangeAirplaneType_success() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        AirplaneTypeEntity newType = createAirplaneType(2L);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(flightRepository.findById(1L)).willReturn(Optional.of(flight));
        given(airplaneTypeService.getAirplaneTypeEntity(2L)).willReturn(newType);
        given(flightRepository.save(flight)).willReturn(flight);
        given(flightMapper.toDto(flight)).willReturn(dto);

        ResponseFlightDto result = flightService.changeAirplaneType(1L, 2L);
        assertThat(result).isEqualTo(dto);
        assertThat(flight.getAirplaneType()).isSameAs(newType);
    }

    @DisplayName("Should throw FlightCanNotChangeAirplaneTypeException when flight is not scheduled")
    @Test
    void shouldThrowWhenChangeAirplaneTypeInvalidStatus() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.BOARDING);

        given(flightRepository.findById(1L)).willReturn(Optional.of(flight));

        assertThrows(FlightCanNotChangeAirplaneTypeException.class, () -> flightService.changeAirplaneType(1L, 2L));
        verify(airplaneTypeService, never()).getAirplaneTypeEntity(any());
    }

    @DisplayName("Should throw DateToBeforeDateFromException when date range invalid")
    @Test
    void shouldThrowWhenGetAllFlightsByRouteAndDates_dateToBeforeDateFrom() {
        assertThrows(DateToBeforeDateFromException.class,
                () -> flightService.getAllFlightsByRouteAndDates(
                        "AV1234",
                        LocalDate.of(2026, 8, 31),
                        LocalDate.of(2026, 8, 1)));
    }

    @DisplayName("Should throw RouteNotActiveException when route is inactive")
    @Test
    void shouldThrowWhenGetAllFlightsByRouteAndDates_routeInactive() {
        RouteEntity route = createRoute("AV1234", "UTC", false);
        given(routeService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteNotActiveException.class,
                () -> flightService.getAllFlightsByRouteAndDates(
                        "AV1234",
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 31)));
    }

    @DisplayName("Should return all flights by route and date range")
    @Test
    void shouldReturnFlightsByRouteAndDates() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), null, FlightStatus.SCHEDULED);

        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightRepository.findAllByRouteAndDepartureDateTimeBetween(eq(route), any(OffsetDateTime.class), any(OffsetDateTime.class))).willReturn(List.of(flight));
        given(flightMapper.toDto(List.of(flight))).willReturn(List.of(dto));

        List<ResponseFlightDto> result = flightService.getAllFlightsByRouteAndDates("AV1234", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
        assertThat(result).containsExactly(dto);
    }

    @DisplayName("Should update flight status to completed when departure is in the past")
    @Test
    void shouldUpdateFlightStatus_toCompleted() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.SCHEDULED);

        boolean updated = flightService.updateFlightStatus(flight, OffsetDateTime.now(ZoneOffset.UTC));

        assertThat(updated).isTrue();
        assertThat(flight.isCompleted()).isTrue();
    }

    @DisplayName("Should update flight status to check-in available")
    @Test
    void shouldUpdateFlightStatus_startCheckIn() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).plusHours(2);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.SCHEDULED);
        OffsetDateTime now = departure.minusHours(2);

        boolean updated = flightService.updateFlightStatus(flight, now);
        assertThat(updated).isTrue();
        assertThat(flight.isCheckInAvailable()).isTrue();
    }

    @DisplayName("Should update flight status to boarding")
    @Test
    void shouldUpdateFlightStatus_startBoarding() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(20);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.CHECK_IN_AVAILABLE);
        OffsetDateTime now = departure.minusMinutes(20);

        boolean updated = flightService.updateFlightStatus(flight, now);
        assertThat(updated).isTrue();
        assertThat(flight.isInBoarding()).isTrue();
    }

    @DisplayName("Should not update flight status when outside any range")
    @Test
    void shouldNotUpdateFlightStatus_whenNoCondition() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.SCHEDULED);
        OffsetDateTime now = departure.minusHours(5);

        boolean updated = flightService.updateFlightStatus(flight, now);
        assertThat(updated).isFalse();
        assertThat(flight.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
    }

    @DisplayName("Should count updated flights when status changes")
    @Test
    void shouldUpdateFlightsStatus_countUpdatedFlights() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5), FlightStatus.SCHEDULED);

        given(flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED)).willReturn(List.of(flight));

        int count = flightService.updateFlightsStatus();
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("Should start global flight generation")
    @Test
    void shouldStartGlobalFlightGeneration() {
        FlightGenerationEntity generation = FlightGenerationEntity.startGlobalGeneration();
        generation.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.GLOBAL, null, null, generation.getStartedAt(), null, null, "/flight-generations/1");

        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);
        given(flightGenerationMapper.toDto(generation)).willReturn(dto);

        ResponseFlightsGenerationDto result = flightService.startGlobalFlightGeneration();
        verify(asyncFlightGenerationService).executeGeneration(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_GlobalGeneration() {
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(FlightGenerationAlreadyRunningException.class, flightService::startGlobalFlightGeneration);
    }

    @DisplayName("Should start route flight generation")
    @Test
    void shouldStartRouteFlightGeneration() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightGenerationEntity generation = FlightGenerationEntity.startRouteGeneration(route.getId());
        generation.setId(1L);
        ResponseFlightsGenerationDto dto = new ResponseFlightsGenerationDto(1L, FlightGenerationStatus.RUNNING, FlightGenerationType.ROUTE, route.getId(), null, generation.getStartedAt(), null, null, "/flight-generations/1");

        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);
        given(flightGenerationMapper.toDto(generation)).willReturn(dto);

        ResponseFlightsGenerationDto result = flightService.startRouteFlightGeneration("AV1234");

        verify(asyncFlightGenerationService).executeGeneration(1L);
        assertThat(result).isEqualTo(dto);
    }

    @DisplayName("Should throw RouteNotActiveException when route generation is requested for inactive route")
    @Test
    void shouldThrowWhenRouteInactive_RouteGeneration() {
        RouteEntity route = createRoute("AV1234", "UTC", false);
        given(routeService.getRouteEntity("AV1234")).willReturn(route);

        assertThrows(RouteNotActiveException.class, () -> flightService.startRouteFlightGeneration("AV1234"));
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_RouteGeneration() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        given(routeService.getRouteEntity("AV1234")).willReturn(route);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(FlightGenerationAlreadyRunningException.class, () -> flightService.startRouteFlightGeneration("AV1234"));
    }


    @DisplayName("Should start daily flight generation")
    @Test
    void shouldStartDailyFlightGeneration() {
        FlightGenerationEntity generation = FlightGenerationEntity.startDailyGeneration(LocalDate.now());
        generation.setId(1L);
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class))).willReturn(generation);

        flightService.startDailyFlightGeneration(LocalDate.now());

        verify(flightGenerationRepository).save(any(FlightGenerationEntity.class));
        verify(asyncFlightGenerationService).executeGeneration(any(Long.class));
    }

    @DisplayName("Should throw FlightGenerationAlreadyRunningException when a generation is already running")
    @Test
    void shouldThrowExceptionWhenGenerationAlreadyRunning_DailyGeneration() {
        given(flightGenerationRepository.save(any(FlightGenerationEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(FlightGenerationAlreadyRunningException.class, () -> flightService.startDailyFlightGeneration(LocalDate.now()));
    }
}

