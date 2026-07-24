package com.falcon.booking.feature.airport.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class AirportExceptionHandler {

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<Error> handleException(AirportNotFoundException exception){
        Error error = new Error("airport-does-not-exist", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
