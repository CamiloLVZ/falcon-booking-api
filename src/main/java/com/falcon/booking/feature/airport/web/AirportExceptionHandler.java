package com.falcon.booking.feature.airport.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.airport.exception.AirportNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AirportExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AirportExceptionHandler.class);

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<Error> handleException(AirportNotFoundException exception){
        Error error = new Error("airport-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
