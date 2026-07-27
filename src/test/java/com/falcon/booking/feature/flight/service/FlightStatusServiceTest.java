package com.falcon.booking.feature.flight.service;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.reservation.service.ReservationCommandService;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import com.falcon.booking.persistence.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FlightStatusServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private com.falcon.booking.feature.boarding.service.BoardingService boardingService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @InjectMocks
    private FlightStatusService flightStatusService;

    @BeforeEach
    void setup() {
        flightStatusService.checkInHoursBeforeToStart = 3;
        flightStatusService.checkInHoursBeforeToClose = 1;
        flightStatusService.boardingMinutesBeforeToStart = 30;
        flightStatusService.boardingMinutesBeforeToClose = 10;
    }

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

    @DisplayName("Should update flight status to completed when departure is in the past")
    @Test
    void shouldUpdateFlightStatus_toCompleted() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.SCHEDULED);

        boolean updated = flightStatusService.updateFlightStatus(flight, OffsetDateTime.now(ZoneOffset.UTC));

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

        boolean updated = flightStatusService.updateFlightStatus(flight, now);
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

        boolean updated = flightStatusService.updateFlightStatus(flight, now);
        assertThat(updated).isTrue();
        assertThat(flight.isInBoarding()).isTrue();
    }

    @DisplayName("Should update flight status to gate closed")
    @Test
    void shouldUpdateFlightStatus_gateClosed() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.BOARDING);
        OffsetDateTime now = departure.minusMinutes(5);

        boolean updated = flightStatusService.updateFlightStatus(flight, now);
        assertThat(updated).isTrue();
        assertThat(flight.isGateClosed()).isTrue();
    }

    @DisplayName("Should not update flight status when outside any range")
    @Test
    void shouldNotUpdateFlightStatus_whenNoCondition() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        OffsetDateTime departure = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);
        FlightEntity flight = createFlight(1L, route, departure, FlightStatus.SCHEDULED);
        OffsetDateTime now = departure.minusHours(5);

        boolean updated = flightStatusService.updateFlightStatus(flight, now);
        assertThat(updated).isFalse();
        assertThat(flight.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
    }

    @DisplayName("Should count updated flights and complete reservations when status changes")
    @Test
    void shouldUpdateFlightsStatus_countUpdatedFlights() {
        RouteEntity route = createRoute("AV1234", "UTC", true);
        FlightEntity flight = createFlight(1L, route, OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5), FlightStatus.SCHEDULED);

        given(flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED)).willReturn(List.of(flight));

        int count = flightStatusService.updateFlightsStatus();
        assertThat(count).isEqualTo(1);
        verify(reservationCommandService).completeReservationsForFlight(flight);
    }

    @DisplayName("Should not complete reservations when no flights to update")
    @Test
    void shouldNotCompleteReservations_whenNoFlights() {
        given(flightRepository.findAllByStatusNotAndStatusNot(FlightStatus.CANCELED, FlightStatus.COMPLETED)).willReturn(List.of());

        flightStatusService.updateFlightsStatus();

        verify(reservationCommandService, org.mockito.Mockito.never())
                .completeReservationsForFlight(any());
    }
}
