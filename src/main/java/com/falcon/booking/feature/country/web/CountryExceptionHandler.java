package com.falcon.booking.feature.country.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CountryExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CountryExceptionHandler.class);

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<Error> handleException(CountryNotFoundException exception){
        Error error = new Error("country-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
