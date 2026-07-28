package com.falcon.booking.feature.airplaneType.web;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import com.falcon.booking.feature.airplaneType.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AirplaneTypeExceptionHandlerTest {

    private final AirplaneTypeExceptionHandler handler = new AirplaneTypeExceptionHandler();

    @Test
    void shouldHandleAirplaneNotFound() {
        var response = handler.handleException(new AirplaneNotFoundException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("airplane-type-does-not-exist");
    }

    @Test
    void shouldHandleAirplaneTypeAlreadyExists() {
        var response = handler.handleException(new AirplaneTypeAlreadyExistsException("AIRBUS", "A320"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("airplane-type-already-exists");
    }

    @Test
    void shouldHandleAirplaneTypeStatusInvalid() {
        var response = handler.handleException(new AirplaneTypeStatusInvalidException("invalid"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("airplane-type-status-invalid");
    }

    @Test
    void shouldHandleAirplaneTypeInvalidStatusChange() {
        var response = handler.handleException(
                new AirplaneTypeInvalidStatusChangeException(AirplaneTypeStatus.ACTIVE, AirplaneTypeStatus.INACTIVE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-status-change");
    }

    @Test
    void shouldHandleInvalidSeatConfiguration() {
        var response = handler.handleException(new InvalidSeatConfigurationException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().type()).isEqualTo("invalid-seat-configuration");
    }

    @Test
    void shouldHandleInvalidSeatNumber() {
        var response = handler.handleException(new InvalidSeatNumberException(1, 10));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-seat-number");
    }
}
