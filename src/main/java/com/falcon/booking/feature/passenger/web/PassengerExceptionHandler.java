package com.falcon.booking.feature.passenger.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.passenger.exception.PassengerAlreadyExistsException;
import com.falcon.booking.feature.passenger.exception.PassengerHasDifferentPassportNumberException;
import com.falcon.booking.feature.passenger.exception.PassengerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PassengerExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PassengerExceptionHandler.class);

    @ExceptionHandler(PassengerAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(PassengerAlreadyExistsException exception){
        Error error = new Error("passenger-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<Error> handleException(PassengerNotFoundException exception){
        Error error = new Error("passenger-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PassengerHasDifferentPassportNumberException.class)
    public ResponseEntity<Error> handleException(PassengerHasDifferentPassportNumberException exception){
        Error error = new Error("passenger-has-different-passport-number", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
