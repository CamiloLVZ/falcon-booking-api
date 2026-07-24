package com.falcon.booking.feature.boarding.web;

import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.boarding.exception.BoardingPassAlreadyBoardedException;
import com.falcon.booking.feature.boarding.exception.BoardingPassExpiredException;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.exception.InvalidBoardingPassengerReservationException;
import com.falcon.booking.feature.boarding.pdf.exception.PdfGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BoardingExceptionHandler {

    @ExceptionHandler(BoardingPassNotFoundException.class)
    public ResponseEntity<Error> handleException(BoardingPassNotFoundException e) {
        Error error = new Error("boarding-pass-not-found", e.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BoardingPassAlreadyBoardedException.class)
    public ResponseEntity<Error> handleException(BoardingPassAlreadyBoardedException e) {
        Error error = new Error("boarding-pass-already-boarded", e.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BoardingPassExpiredException.class)
    public ResponseEntity<Error> handleException(BoardingPassExpiredException e) {
        Error error = new Error("boarding-pass-expired", e.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidBoardingPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidBoardingPassengerReservationException e) {
        Error error = new Error("invalid-boarding-passenger-reservation", e.getMessage());
        log.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<Error> handleException(PdfGenerationException e) {
        Error error = new Error("pdf-generation-error", e.getMessage());
        log.error(error.message());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
