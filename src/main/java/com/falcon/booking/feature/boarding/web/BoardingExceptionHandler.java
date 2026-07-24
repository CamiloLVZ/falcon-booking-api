package com.falcon.booking.feature.boarding.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.boarding.exception.BoardingPassAlreadyBoardedException;
import com.falcon.booking.feature.boarding.exception.BoardingPassExpiredException;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.exception.InvalidBoardingPassengerReservationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BoardingExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(BoardingExceptionHandler.class);

    @ExceptionHandler(BoardingPassNotFoundException.class)
    public ResponseEntity<Error> handleException(BoardingPassNotFoundException e) {
        Error error = new Error("boarding-pass-not-found", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BoardingPassAlreadyBoardedException.class)
    public ResponseEntity<Error> handleException(BoardingPassAlreadyBoardedException e) {
        Error error = new Error("boarding-pass-already-boarded", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BoardingPassExpiredException.class)
    public ResponseEntity<Error> handleException(BoardingPassExpiredException e) {
        Error error = new Error("boarding-pass-expired", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidBoardingPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidBoardingPassengerReservationException e) {
        Error error = new Error("invalid-boarding-passenger-reservation", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
