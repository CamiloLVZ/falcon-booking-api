package com.falcon.booking.feature.airport.web;

import com.falcon.booking.feature.airport.exception.AirportAlreadyExistsException;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.DateTimeException;

import static org.assertj.core.api.Assertions.assertThat;

class AirportExceptionHandlerTest {

    private final AirportExceptionHandler handler = new AirportExceptionHandler();

    @Test
    void shouldHandleAirportNotFound() {
        var response = handler.handleException(new AirportNotFoundException("BOG"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("airport-does-not-exist");
    }

    @Test
    void shouldHandleAirportAlreadyExists() {
        var response = handler.handleException(new AirportAlreadyExistsException("BOG"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("airport-already-exists");
    }

    @Test
    void shouldHandleDateTimeException() {
        var response = handler.handleException(new DateTimeException("Invalid timezone"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-timezone");
    }
}
