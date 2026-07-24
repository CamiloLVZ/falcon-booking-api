package com.falcon.booking.feature.checkIn.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.checkIn.exception.InvalidCheckInPassengerReservationStatusException;
import com.falcon.booking.feature.checkIn.exception.SeatNumberAlreadyTakenException;
import com.falcon.booking.feature.checkIn.exception.SeatNumberOutOfRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CheckInExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CheckInExceptionHandler.class);

    @ExceptionHandler(SeatNumberAlreadyTakenException.class)
    public ResponseEntity<Error> handleException(SeatNumberAlreadyTakenException e) {
        Error error = new Error("seat-already-taken", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidCheckInPassengerReservationStatusException.class)
    public ResponseEntity<Error> handleException(InvalidCheckInPassengerReservationStatusException e) {
        Error error = new Error("invalid-check-in-reservation-status", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SeatNumberOutOfRangeException.class)
    public ResponseEntity<Error> handleException(SeatNumberOutOfRangeException e) {
        Error error = new Error("seat-number-out-of-range", e.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
