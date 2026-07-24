package com.falcon.booking.feature.reservation.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.reservation.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ReservationExceptionHandler {

    @ExceptionHandler(DuplicateSeatNumberInReservationException.class)
    public ResponseEntity<Error> handleException(DuplicateSeatNumberInReservationException exception){
        Error error = new Error("seat-duplicated-in-request", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationMustHavePassengersException.class)
    public ResponseEntity<Error> handleException(ReservationMustHavePassengersException exception){
        Error error = new Error("reservation-with-no-passengers", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Error> handleException(ReservationNotFoundException exception){
        Error error = new Error("reservation-does-not-exist", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PassengerNotFoundInReservationException.class)
    public ResponseEntity<Error> handleException(PassengerNotFoundInReservationException exception){
        Error error = new Error("passenger-not-found-in-reservation", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FlightCapacityExceededException.class)
    public ResponseEntity<Error> handleException(FlightCapacityExceededException exception){
        Error error = new Error("flight-capacity-exceeded", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicatedPassengerException.class)
    public ResponseEntity<Error> handleException(DuplicatedPassengerException exception){
        Error error = new Error("duplicate-passenger", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerAlreadyReservedFlightException.class)
    public ResponseEntity<Error> handleException(PassengerAlreadyReservedFlightException exception){
        Error error = new Error("passenger-already-reserved-flight", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(ReservationInvalidStatusChangeException exception){
        Error error = new Error("reservation-invalid-status-change", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerReservationNotFoundException.class)
    public ResponseEntity<Error> handleException(PassengerReservationNotFoundException exception){
        Error error = new Error("passenger-reservation-does-not-exist", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}


