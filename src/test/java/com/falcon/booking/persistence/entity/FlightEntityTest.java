package com.falcon.booking.persistence.entity;

import com.falcon.booking.domain.exception.Flight.FlightInvalidStatusChangeException;
import com.falcon.booking.domain.valueobject.FlightStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightEntityTest {

    private FlightEntity createFlight(FlightStatus status, OffsetDateTime departureDateTime) {
        FlightEntity flight = new FlightEntity();
        flight.setStatus(status);
        flight.setDepartureDateTime(departureDateTime);
        return flight;
    }

    @DisplayName("Should cancel scheduled flight")
    @Test
    void shouldCancelFlight_cancel() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, OffsetDateTime.now().plusHours(2));

        flight.cancel();

        assertThat(flight.isCanceled()).isTrue();
    }

    @DisplayName("Should throw exception when canceling completed flight")
    @Test
    void shouldThrowException_cancel() {
        FlightEntity flight = createFlight(FlightStatus.COMPLETED, OffsetDateTime.now().minusHours(1));

        FlightInvalidStatusChangeException exception =
                assertThrows(FlightInvalidStatusChangeException.class, flight::cancel);

        assertThat(exception.getMessage()).contains("COMPLETED to CANCELED");
    }

    @DisplayName("Should start check in from scheduled status")
    @Test
    void shouldStartCheckIn_startCheckIn() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, OffsetDateTime.now().plusHours(5));

        flight.startCheckIn();

        assertThat(flight.isCheckInAvailable()).isTrue();
    }

    @DisplayName("Should start boarding from check-in available status")
    @Test
    void shouldStartBoarding_startBoarding() {
        FlightEntity flight = createFlight(FlightStatus.CHECK_IN_AVAILABLE, OffsetDateTime.now().plusHours(2));

        flight.startBoarding();

        assertThat(flight.isInBoarding()).isTrue();
    }

    @DisplayName("Should throw exception when boarding starts from scheduled status")
    @Test
    void shouldThrowException_startBoarding() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, OffsetDateTime.now().plusHours(2));

        assertThrows(FlightInvalidStatusChangeException.class, flight::startBoarding);
    }

    @DisplayName("Should mark flight as completed from boarding status")
    @Test
    void shouldMarkAsCompleted_markAsCompleted() {
        FlightEntity flight = createFlight(FlightStatus.BOARDING, OffsetDateTime.now().minusMinutes(10));

        flight.markAsCompleted();

        assertThat(flight.isCompleted()).isTrue();
    }

    @DisplayName("Should correct status to completed when now is after departure")
    @Test
    void shouldCorrectStatusByTime_correctStatusByTime() {
        FlightEntity flight = createFlight(FlightStatus.SCHEDULED, OffsetDateTime.now().minusMinutes(5));

        flight.correctStatusByTime(OffsetDateTime.now());

        assertThat(flight.isCompleted()).isTrue();
    }

    @DisplayName("Should keep canceled status when correcting by time")
    @Test
    void shouldKeepCanceledStatus_correctStatusByTime() {
        FlightEntity flight = createFlight(FlightStatus.CANCELED, OffsetDateTime.now().minusMinutes(5));

        flight.correctStatusByTime(OffsetDateTime.now());

        assertThat(flight.isCanceled()).isTrue();
    }

    @DisplayName("Equals methods for entities with same id should return true")
    @Test
    void shouldReturnTrue_sameFlightInEquals() {
        FlightEntity flight1 = new FlightEntity();
        flight1.setId(1L);

        FlightEntity flight2 = new FlightEntity();
        flight2.setId(1L);

        assertThat(flight1.equals(flight2)).isTrue();
    }

    @DisplayName("Hash code for entities with different id should not be equal")
    @Test
    void shouldNotBeEqual_differentFlightHashCode() {
        FlightEntity flight1 = new FlightEntity();
        flight1.setId(1L);

        FlightEntity flight2 = new FlightEntity();
        flight2.setId(2L);

        assertThat(flight1.hashCode()).isNotEqualTo(flight2.hashCode());
    }
}
