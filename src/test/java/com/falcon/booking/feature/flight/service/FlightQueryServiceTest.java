package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.*;
import com.falcon.booking.feature.airport.service.AirportService;
import com.falcon.booking.feature.flight.dto.FlightSeatMapDto;
import com.falcon.booking.feature.flight.dto.ResponseFlightDto;
import com.falcon.booking.feature.flight.exception.FlightNotFoundException;
import com.falcon.booking.feature.flight.mapper.FlightMapper;
import com.falcon.booking.feature.payment.service.PricingService;
import com.falcon.booking.feature.route.exception.RouteNotActiveException;
import com.falcon.booking.feature.route.service.RouteQueryService;
import com.falcon.booking.persistence.entity.*;
import com.falcon.booking.persistence.repository.FlightRepository;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
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
    @Mock private PassengerReservationRepository passengerReservationRepository;
    @Mock private PricingService pricingService;

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
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", entity.getDepartureDateTime(), entity.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

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
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedFlight.getDepartureDateTime(), expectedFlight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        Page<FlightEntity> flightPage = new PageImpl<>(List.of(expectedFlight), PageRequest.of(0, 10, Sort.by(FlightQueryService.SORT_FIELD_DEPARTURE_DATE_TIME).ascending()), 1);
        given(flightRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by(FlightQueryService.SORT_FIELD_DEPARTURE_DATE_TIME).ascending())))).willReturn(flightPage);
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
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", flight.getDepartureDateTime(), flight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        given(routeQueryService.getRouteEntity("AV1234")).willReturn(route);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(FlightQueryService.SORT_FIELD_DEPARTURE_DATE_TIME).ascending());
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
        ResponseFlightDto dto = new ResponseFlightDto(1L, "AV1234", "BOG", "BOG", expectedFlight.getDepartureDateTime(), expectedFlight.getDepartureDateTime().toLocalDateTime(), 40, null, FlightStatus.SCHEDULED, BigDecimal.valueOf(100.0), BigDecimal.valueOf(200.0));

        Page<FlightEntity> flightPage = new PageImpl<>(List.of(expectedFlight), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, FlightQueryService.SORT_FIELD_DEPARTURE_DATE_TIME)), 1);
        given(flightRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, FlightQueryService.SORT_FIELD_DEPARTURE_DATE_TIME))))).willReturn(flightPage);
        given(flightMapper.toDto(expectedFlight)).willReturn(dto);

        Page<ResponseFlightDto> result = flightQueryService.getAllFlightsPaginated("AV1234", FlightStatus.SCHEDULED, 0, 10);
        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // getSeatMap tests
    // -------------------------------------------------------------------------

    @DisplayName("Should return seat map with all seats AVAILABLE when flight has no check-ins")
    @Test
    void shouldReturnSeatMap_allAvailable() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        // airplane: 6 economy seats, 0 first class, columns "ABC" -> 2 rows of 3
        AirplaneTypeEntity airplane = new AirplaneTypeEntity();
        airplane.configureSeats(6, 0, "ABC");
        FlightEntity flight = new FlightEntity(route, airplane, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.CHECK_IN_AVAILABLE);
        flight.setId(10L);

        given(flightRepository.findById(10L)).willReturn(Optional.of(flight));
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(pricingService.calculatePrice(flight, SeatClass.ECONOMY)).willReturn(new BigDecimal("350.00"));
        given(pricingService.calculatePrice(flight, SeatClass.FIRST_CLASS)).willReturn(BigDecimal.ZERO);

        FlightSeatMapDto result = flightQueryService.getSeatMap(10L);

        assertThat(result).isNotNull();
        assertThat(result.seatColumns()).isEqualTo("ABC");
        assertThat(result.firstClassRows()).isEqualTo(0);
        assertThat(result.economyRows()).isEqualTo(2);
        assertThat(result.priceEconomy()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(result.seats()).hasSize(6);
        assertThat(result.seats()).allMatch(s -> s.status() == SeatStatus.AVAILABLE);
        assertThat(result.seats()).allMatch(s -> s.seatClass() == SeatClass.ECONOMY);
    }

    @DisplayName("Should mark seat as OCCUPIED when a passenger reservation holds it")
    @Test
    void shouldReturnSeatMap_seatOccupied() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        AirplaneTypeEntity airplane = new AirplaneTypeEntity();
        airplane.configureSeats(6, 0, "ABC");
        FlightEntity flight = new FlightEntity(route, airplane, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.CHECK_IN_AVAILABLE);
        flight.setId(10L);

        // Simulate a PassengerReservationEntity occupying seat 2
        PassengerReservationEntity occupiedReservation = new PassengerReservationEntity();
        ReflectionTestUtils.setField(occupiedReservation, "seatNumber", 2);
        ReflectionTestUtils.setField(occupiedReservation, "status", PassengerReservationStatus.CHECKED_IN);

        given(flightRepository.findById(10L)).willReturn(Optional.of(flight));
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of(occupiedReservation));
        given(pricingService.calculatePrice(flight, SeatClass.ECONOMY)).willReturn(new BigDecimal("350.00"));
        given(pricingService.calculatePrice(flight, SeatClass.FIRST_CLASS)).willReturn(BigDecimal.ZERO);

        FlightSeatMapDto result = flightQueryService.getSeatMap(10L);

        assertThat(result.seats().get(1).status()).isEqualTo(SeatStatus.OCCUPIED); // seat number 2 -> index 1
        assertThat(result.seats().get(0).status()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(result.seats().get(2).status()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @DisplayName("Should ignore reservations with null seatNumber when computing occupied seats")
    @Test
    void shouldReturnSeatMap_ignoresNullSeatNumbers() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        AirplaneTypeEntity airplane = new AirplaneTypeEntity();
        airplane.configureSeats(3, 0, "ABC");
        FlightEntity flight = new FlightEntity(route, airplane, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        flight.setId(11L);

        // Reservation with no seat assigned yet
        PassengerReservationEntity reservedNoSeat = new PassengerReservationEntity();
        ReflectionTestUtils.setField(reservedNoSeat, "seatNumber", null);
        ReflectionTestUtils.setField(reservedNoSeat, "status", PassengerReservationStatus.RESERVED);

        given(flightRepository.findById(11L)).willReturn(Optional.of(flight));
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of(reservedNoSeat));
        given(pricingService.calculatePrice(flight, SeatClass.ECONOMY)).willReturn(new BigDecimal("300.00"));
        given(pricingService.calculatePrice(flight, SeatClass.FIRST_CLASS)).willReturn(BigDecimal.ZERO);

        FlightSeatMapDto result = flightQueryService.getSeatMap(11L);

        assertThat(result.seats()).allMatch(s -> s.status() == SeatStatus.AVAILABLE);
    }

    @DisplayName("Should assign correct price per seat class in mixed-class airplane")
    @Test
    void shouldReturnSeatMap_correctPricePerSeatClass() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        AirplaneTypeEntity airplane = new AirplaneTypeEntity();
        // 3 first-class seats (1-3) + 6 economy seats (4-9), columns "ABC"
        airplane.configureSeats(6, 3, "ABC");
        FlightEntity flight = new FlightEntity(route, airplane, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        flight.setId(12L);

        BigDecimal economyPrice = new BigDecimal("350.00");
        BigDecimal firstClassPrice = new BigDecimal("1200.00");

        given(flightRepository.findById(12L)).willReturn(Optional.of(flight));
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(pricingService.calculatePrice(flight, SeatClass.ECONOMY)).willReturn(economyPrice);
        given(pricingService.calculatePrice(flight, SeatClass.FIRST_CLASS)).willReturn(firstClassPrice);

        FlightSeatMapDto result = flightQueryService.getSeatMap(12L);

        assertThat(result.priceEconomy()).isEqualByComparingTo(economyPrice);
        assertThat(result.priceFirstClass()).isEqualByComparingTo(firstClassPrice);
        // seats 1-3 are first class
        assertThat(result.seats().subList(0, 3))
                .allMatch(s -> s.seatClass() == SeatClass.FIRST_CLASS)
                .allMatch(s -> s.price().compareTo(firstClassPrice) == 0);
        // seats 4-9 are economy
        assertThat(result.seats().subList(3, 9))
                .allMatch(s -> s.seatClass() == SeatClass.ECONOMY)
                .allMatch(s -> s.price().compareTo(economyPrice) == 0);
    }

    @DisplayName("Should throw FlightNotFoundException when getSeatMap flight does not exist")
    @Test
    void shouldThrowException_getSeatMap_whenFlightNotFound() {
        given(flightRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(FlightNotFoundException.class, () -> flightQueryService.getSeatMap(99L));
    }

    @DisplayName("Should return correct seat labels in seat map")
    @Test
    void shouldReturnSeatMap_correctSeatLabels() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        AirplaneTypeEntity airplane = new AirplaneTypeEntity();
        airplane.configureSeats(6, 0, "ABC"); // rows: 1A,1B,1C, 2A,2B,2C
        FlightEntity flight = new FlightEntity(route, airplane, OffsetDateTime.now(ZoneOffset.UTC), FlightStatus.SCHEDULED);
        flight.setId(13L);

        given(flightRepository.findById(13L)).willReturn(Optional.of(flight));
        given(passengerReservationRepository.findAllByFlight(flight)).willReturn(List.of());
        given(pricingService.calculatePrice(flight, SeatClass.ECONOMY)).willReturn(BigDecimal.TEN);
        given(pricingService.calculatePrice(flight, SeatClass.FIRST_CLASS)).willReturn(BigDecimal.ZERO);

        FlightSeatMapDto result = flightQueryService.getSeatMap(13L);

        assertThat(result.seats().get(0).label()).isEqualTo("1A");
        assertThat(result.seats().get(1).label()).isEqualTo("1B");
        assertThat(result.seats().get(2).label()).isEqualTo("1C");
        assertThat(result.seats().get(3).label()).isEqualTo("2A");
        assertThat(result.seats().get(5).label()).isEqualTo("2C");
    }
}
