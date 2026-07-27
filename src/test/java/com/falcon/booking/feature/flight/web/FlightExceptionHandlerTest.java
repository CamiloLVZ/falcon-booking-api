package com.falcon.booking.feature.flight.web;

import com.falcon.booking.common.enums.FlightStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.flight.exception.*;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationAlreadyRunningException;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationNotFoundException;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationPartialFailureException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlightExceptionHandlerTest {

    private final FlightExceptionHandler handler = new FlightExceptionHandler();

    @Test
    void shouldHandleFlightNotFound() {
        var response = handler.handleException(new FlightNotFoundException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("flight-does-not-exist");
    }

    @Test
    void shouldHandleFlightAlreadyExists() {
        var response = handler.handleException(new FlightAlreadyExistsException("AV123", OffsetDateTime.now()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-already-exists");
    }

    @Test
    void shouldHandleFlightCanNotChangeAirplaneType() {
        var response = handler.handleException(new FlightCanNotChangeAirplaneTypeException(FlightStatus.SCHEDULED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-can-not-change-airplane-type");
    }

    @Test
    void shouldHandleFlightCanNotBeReserved() {
        var response = handler.handleException(new FlightCanNotBeReservedException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-not-able-to-make-reservations");
    }

    @Test
    void shouldHandleFlightCanNotBeRescheduled() {
        var response = handler.handleException(new FlightCanNotBeRescheduledException(FlightStatus.SCHEDULED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-can-not-be-re-scheduled");
    }

    @Test
    void shouldHandleFlightGenerationAlreadyRunning() {
        var response = handler.handleException(new FlightGenerationAlreadyRunningException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-generation-already-running");
    }

    @Test
    void shouldHandleFlightGenerationNotFound() {
        var response = handler.handleException(new FlightGenerationNotFoundException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("flight-generation-does-not-exist");
    }

    @Test
    void shouldHandleFlightInvalidStatusChange() {
        var response = handler.handleException(new FlightInvalidStatusChangeException(FlightStatus.SCHEDULED, FlightStatus.CANCELED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-invalid-status-change");
    }

    @Test
    void shouldHandleFlightGenerationPartialFailure() {
        var response = handler.handleException(new FlightGenerationPartialFailureException(List.of(1L)));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().type()).isEqualTo("flight-generation-partial-failure");
    }

    @Test
    void shouldHandleOutOfFlightCheckInTime() {
        var response = handler.handleException(new OutOfFlightCheckInTimeException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-out-of-check-in-time");
    }

    @Test
    void shouldHandleOutOfFlightBoardingTime() {
        var response = handler.handleException(new OutOfFlightBoardingTimeException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("flight-out-of-boarding-time");
    }
}
