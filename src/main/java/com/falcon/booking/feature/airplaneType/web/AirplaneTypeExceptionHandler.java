package com.falcon.booking.feature.airplaneType.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.airplaneType.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AirplaneTypeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AirplaneTypeExceptionHandler.class);

    @ExceptionHandler(AirplaneNotFoundException.class)
    public ResponseEntity<Error> handleException(AirplaneNotFoundException exception) {
        Error error = new Error("airplane-type-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirplaneTypeAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeAlreadyExistsException exception) {
        Error error = new Error("airplane-type-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeStatusInvalidException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeStatusInvalidException exception) {
        Error error = new Error("airplane-type-status-invalid", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeInvalidStatusChangeException exception) {
        Error error = new Error("invalid-status-change", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidSeatConfigurationException.class)
    public ResponseEntity<Error> handleException(InvalidSeatConfigurationException exception) {
        Error error = new Error("invalid-seat-configuration", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(InvalidSeatNumberException.class)
    public ResponseEntity<Error> handleException(InvalidSeatNumberException exception) {
        Error error = new Error("invalid-seat-number", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
