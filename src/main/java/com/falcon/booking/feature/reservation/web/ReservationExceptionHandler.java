package com.falcon.booking.feature.reservation.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.reservation.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationExceptionHandler.class);

    @ExceptionHandler(SeatNumberAlreadyTakenException.class)
    public ResponseEntity<Error> handleException(SeatNumberAlreadyTakenException exception){
        Error error = new Error("seat-is-already-taken", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SeatNumberOutOfRangeException.class)
    public ResponseEntity<Error> handleException(SeatNumberOutOfRangeException exception){
        Error error = new Error("seat-number-out-of-range", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicateSeatNumberInReservationException.class)
    public ResponseEntity<Error> handleException(DuplicateSeatNumberInReservationException exception){
        Error error = new Error("seat-duplicated-in-request", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationMustHavePassengersException.class)
    public ResponseEntity<Error> handleException(ReservationMustHavePassengersException exception){
        Error error = new Error("reservation-with-no-passengers", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Error> handleException(ReservationNotFoundException exception){
        Error error = new Error("reservation-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PassengerNotFoundInReservationException.class)
    public ResponseEntity<Error> handleException(PassengerNotFoundInReservationException exception){
        Error error = new Error("passenger-not-found-in-reservation", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCheckInPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidCheckInPassengerReservationException exception){
        Error error = new Error("invalid-status-for-check-in", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidBoardingPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidBoardingPassengerReservationException exception){
        Error error = new Error("invalid-status-for-boarding", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
