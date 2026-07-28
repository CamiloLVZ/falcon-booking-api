package com.falcon.booking.feature.country.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.country.exception.CountryAlreadyExistsException;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CountryExceptionHandler {

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<Error> handleException(CountryNotFoundException exception){
        Error error = new Error("country-does-not-exist", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CountryAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(CountryAlreadyExistsException exception){
        Error error = new Error("country-already-exists", exception.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
