package com.falcon.booking.feature.reservation.web;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.reservation.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationExceptionHandlerTest {

    private final ReservationExceptionHandler handler = new ReservationExceptionHandler();

    @Test
    void shouldHandleDuplicateSeatNumberInReservation() {
        var response = handler.handleException(new DuplicateSeatNumberInReservationException(5));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("seat-duplicated-in-request");
    }

    @Test
    void shouldHandleReservationMustHavePassengers() {
        var response = handler.handleException(new ReservationMustHavePassengersException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("reservation-with-no-passengers");
    }

    @Test
    void shouldHandleReservationNotFound() {
        var response = handler.handleException(new ReservationNotFoundException("RES-001"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("reservation-does-not-exist");
    }

    @Test
    void shouldHandleInvalidReservationAccess() {
        var response = handler.handleException(new InvalidReservationAccessException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("invalid-reservation-access");
    }

    @Test
    void shouldHandlePassengerNotFoundInReservation() {
        var response = handler.handleException(
                new PassengerNotFoundInReservationException("id", "CO", "RES-001"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("passenger-not-found-in-reservation");
    }

    @Test
    void shouldHandleFlightCapacityExceeded() {
        var response = handler.handleException(new FlightCapacityExceededException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-capacity-exceeded");
    }

    @Test
    void shouldHandleDuplicatedPassenger() {
        var response = handler.handleException(new DuplicatedPassengerException("id123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("duplicate-passenger");
    }

    @Test
    void shouldHandlePassengerAlreadyReservedFlight() {
        var response = handler.handleException(new PassengerAlreadyReservedFlightException("id123", 1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("passenger-already-reserved-flight");
    }

    @Test
    void shouldHandleReservationInvalidStatusChange() {
        var response = handler.handleException(
                new ReservationInvalidStatusChangeException(PassengerReservationStatus.RESERVED, PassengerReservationStatus.CANCELED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("reservation-invalid-status-change");
    }

    @Test
    void shouldHandlePassengerReservationNotFound() {
        var response = handler.handleException(new PassengerReservationNotFoundException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("passenger-reservation-does-not-exist");
    }

    @Test
    void shouldHandleReservationCancellationTimeExpired() {
        var response = handler.handleException(new ReservationCancellationTimeExpiredException("RES-001", 24));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("reservation-cancellation-time-expired");
    }
}
