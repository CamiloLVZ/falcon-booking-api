package com.falcon.booking.web.exception;

import com.falcon.booking.domain.exception.CountryDoesNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CountryDoesNotExistException.class)
    public ResponseEntity<Error> handleException(CountryDoesNotExistException exception){
        Error error = new Error("country-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

}
