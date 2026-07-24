package com.falcon.booking.feature.flight.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.flight.exception.*;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationAlreadyRunningException;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationNotFoundException;
import com.falcon.booking.feature.flightGeneration.exception.FlightGenerationPartialFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FlightExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlightExceptionHandler.class);

    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<Error> handleException(FlightNotFoundException exception) {
        Error error = new Error("flight-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FlightAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(FlightAlreadyExistsException exception){
        Error error = new Error("flight-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotChangeAirplaneTypeException.class)
    public ResponseEntity<Error> handleException(FlightCanNotChangeAirplaneTypeException exception){
        Error error = new Error("flight-can-not-change-airplane-type", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotBeReservedException.class)
    public ResponseEntity<Error> handleException(FlightCanNotBeReservedException exception){
        Error error = new Error("flight-not-able-to-make-reservations", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotBeRescheduledException.class)
    public ResponseEntity<Error> handleException(FlightCanNotBeRescheduledException exception){
        Error error = new Error("flight-can-not-be-re-scheduled", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightGenerationAlreadyRunningException.class)
    public ResponseEntity<Error> handleException(FlightGenerationAlreadyRunningException exception){
        Error error = new Error("flight-generation-already-running", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightGenerationNotFoundException.class)
    public ResponseEntity<Error> handleException(FlightGenerationNotFoundException exception){
        Error error = new Error("flight-generation-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FlightInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(FlightInvalidStatusChangeException exception){
        Error error = new Error("flight-invalid-status-change", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightGenerationPartialFailureException.class)
    public ResponseEntity<Error> handleException(FlightGenerationPartialFailureException exception){
        Error error = new Error("flight-generation-partial-failure", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(OutOfFlightCheckInTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightCheckInTimeException exception){
        Error error = new Error("flight-out-of-check-in-time", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OutOfFlightBoardingTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightBoardingTimeException exception){
        Error error = new Error("flight-out-of-boarding-time", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
