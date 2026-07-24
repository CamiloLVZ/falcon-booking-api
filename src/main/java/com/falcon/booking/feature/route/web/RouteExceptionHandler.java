package com.falcon.booking.feature.route.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.route.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class RouteExceptionHandler {

    @ExceptionHandler(RouteAirplaneTypeIsNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteAirplaneTypeIsNotActiveException exception){
        Error error = new Error("route-airplane-type-is-not-active", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(RouteAlreadyExistsException exception){
        Error error = new Error("route-already-exists", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Error> handleException(RouteNotFoundException exception){
        Error error = new Error("route-does-not-exists", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RouteInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(RouteInvalidStatusChangeException exception){
        Error error = new Error("route-invalid-status-change", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteSameOriginAndDestinationException.class)
    public ResponseEntity<Error> handleException(RouteSameOriginAndDestinationException exception){
        Error error = new Error("route-same-origin-and-destination", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteStatusInvalidException.class)
    public ResponseEntity<Error> handleException(RouteStatusInvalidException exception){
        Error error = new Error("route-status-invalid", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteDraftInvalidUpdateException.class)
    public ResponseEntity<Error> handleException(RouteDraftInvalidUpdateException exception){
        Error error = new Error("route-can-not-change-origin-or-destination", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteDayOfWeekInvalidException.class)
    public ResponseEntity<Error> handleException(RouteDayOfWeekInvalidException exception){
        Error error = new Error("route-week-day-invalid", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteNotActiveException exception) {
        Error error = new Error("route-not-active", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteHasNotSchedulesToGenerateFlightsException.class)
    public ResponseEntity<Error> handleException(RouteHasNotSchedulesToGenerateFlightsException exception) {
        Error error = new Error("route-has-not-schedules-for-flights", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidRouteStatusForFlightGenerationException.class)
    public ResponseEntity<Error> handleException(InvalidRouteStatusForFlightGenerationException exception) {
        Error error = new Error("invalid-route-status-to-generate-flights", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotActivableException.class)
    public ResponseEntity<Error> handleException(RouteNotActivableException exception) {
        Error error = new Error("route-is-not-activable", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
