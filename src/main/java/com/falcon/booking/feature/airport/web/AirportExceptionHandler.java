package com.falcon.booking.feature.airport.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.airport.exception.AirportAlreadyExistsException;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;

@RestControllerAdvice
@Slf4j
public class AirportExceptionHandler {

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<Error> handleException(AirportNotFoundException exception){
        Error error = new Error("airport-does-not-exist", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirportAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(AirportAlreadyExistsException exception){
        Error error = new Error("airport-already-exists", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<Error> handleException(DateTimeException exception){
        Error error = new Error("invalid-timezone", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
