package com.falcon.booking.feature.route.web;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.route.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class RouteExceptionHandlerTest {

    private final RouteExceptionHandler handler = new RouteExceptionHandler();

    @Test
    void shouldHandleRouteAirplaneTypeIsNotActive() {
        var response = handler.handleException(new RouteAirplaneTypeIsNotActiveException(1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-airplane-type-is-not-active");
    }

    @Test
    void shouldHandleRouteAlreadyExists() {
        var response = handler.handleException(new RouteAlreadyExistsException("AV123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-already-exists");
    }

    @Test
    void shouldHandleRouteNotFound() {
        var response = handler.handleException(new RouteNotFoundException((Long) 1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("route-does-not-exists");
    }

    @Test
    void shouldHandleRouteInvalidStatusChange() {
        var response = handler.handleException(new RouteInvalidStatusChangeException(RouteStatus.DRAFT, RouteStatus.ACTIVE));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-invalid-status-change");
    }

    @Test
    void shouldHandleRouteSameOriginAndDestination() {
        var response = handler.handleException(new RouteSameOriginAndDestinationException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-same-origin-and-destination");
    }

    @Test
    void shouldHandleRouteStatusInvalid() {
        var response = handler.handleException(new RouteStatusInvalidException("invalid"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-status-invalid");
    }

    @Test
    void shouldHandleRouteDraftInvalidUpdate() {
        var response = handler.handleException(new RouteDraftInvalidUpdateException("AV123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-can-not-change-origin-or-destination");
    }

    @Test
    void shouldHandleRouteDayOfWeekInvalid() {
        var response = handler.handleException(new RouteDayOfWeekInvalidException("MONDAY"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-week-day-invalid");
    }

    @Test
    void shouldHandleRouteNotActive() {
        var response = handler.handleException(new RouteNotActiveException("AV123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-not-active");
    }

    @Test
    void shouldHandleRouteHasNotSchedulesToGenerateFlights() {
        var response = handler.handleException(new RouteHasNotSchedulesToGenerateFlightsException("AV123"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-has-not-schedules-for-flights");
    }

    @Test
    void shouldHandleInvalidRouteStatusForFlightGeneration() {
        var response = handler.handleException(new InvalidRouteStatusForFlightGenerationException(RouteStatus.DRAFT));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-route-status-to-generate-flights");
    }

    @Test
    void shouldHandleRouteNotActivable() {
        var response = handler.handleException(new RouteNotActivableException("test error"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("route-is-not-activable");
    }
}
