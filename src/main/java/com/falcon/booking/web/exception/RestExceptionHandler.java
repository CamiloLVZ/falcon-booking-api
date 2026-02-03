package com.falcon.booking.web.exception;

import com.falcon.booking.domain.exception.*;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeDoesNotExistException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeStatusInvalidException;
import com.falcon.booking.domain.exception.Flight.*;
import com.falcon.booking.domain.exception.Passenger.PassengerAlreadyExistsException;
import com.falcon.booking.domain.exception.Passenger.PassengerDoesNotExistException;
import com.falcon.booking.domain.exception.Passenger.PassengerHasDifferentPassportNumberException;
import com.falcon.booking.domain.exception.Reservation.*;
import com.falcon.booking.domain.exception.Route.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Error>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        List<Error> errors = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().forEach(error->{
            errors.add(new Error(error.getField(), error.getDefaultMessage()));
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Error> handleException(MissingServletRequestParameterException exception){
        Error error = new Error("required-parameter-not-found", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Error> handleException(MethodArgumentTypeMismatchException exception){
        Error error = new Error("invalid-argument-type", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Error> handleValidationExceptions(ConstraintViolationException exception) {

        String firstMessage = exception.getConstraintViolations().iterator().next().getMessage();

        Error error = new Error("invalid-arguments", firstMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ResponseEntity<Error> handleException(InvalidSearchCriteriaException exception){
        Error error = new Error("invalid-search-criteria", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CountryDoesNotExistException.class)
    public ResponseEntity<Error> handleException(CountryDoesNotExistException exception){
        Error error = new Error("country-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirportDoesNotExistException.class)
    public ResponseEntity<Error> handleException(AirportDoesNotExistException exception){
        Error error = new Error("airport-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirplaneTypeDoesNotExistException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeDoesNotExistException exception){
        Error error = new Error("airplane-type-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirplaneTypeAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeAlreadyExistsException exception){
        Error error = new Error("airplane-type-already-exists", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeStatusInvalidException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeStatusInvalidException exception){
            Error error = new Error("airplane-type-status-invalid", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeInvalidStatusChangeException exception){
        Error error = new Error("invalid-status-change", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteAirplaneTypeIsNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteAirplaneTypeIsNotActiveException exception){
        Error error = new Error("route-airplane-type-is-not-active", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(RouteAlreadyExistsException exception){
        Error error = new Error("route-already-exists", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteDoesNotExistException.class)
    public ResponseEntity<Error> handleException(RouteDoesNotExistException exception){
        Error error = new Error("route-does-not-exists", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RouteInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(RouteInvalidStatusChangeException exception){
        Error error = new Error("route-invalid-status-change", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteSameOriginAndDestinationException.class)
    public ResponseEntity<Error> handleException(RouteSameOriginAndDestinationException exception){
        Error error = new Error("route-same-origin-and-destination", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteStatusInvalidException.class)
    public ResponseEntity<Error> handleException(RouteStatusInvalidException exception){
        Error error = new Error("route-status-invalid", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteDraftInvalidUpdateException.class)
    public ResponseEntity<Error> handleException(RouteDraftInvalidUpdateException exception){
        Error error = new Error("route-can-not-change-origin-or-destination", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteWeekDayInvalidException.class)
    public ResponseEntity<Error> handleException(RouteWeekDayInvalidException exception){
        Error error = new Error("route-week-day-invalid", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Error> handleException(HttpMessageNotReadableException exception) {
        Error error = new Error("data-format-invalid", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightDoesNotExistException.class)
    public ResponseEntity<Error> handleException(FlightDoesNotExistException exception) {
        Error error = new Error("flight-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteNotActiveException exception) {
        Error error = new Error("route-not-active", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DateToBeforeDateFromException.class)
    public ResponseEntity<Error> handleException(DateToBeforeDateFromException exception){
        Error error = new Error("date-to-before-date-from", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(FlightAlreadyExistsException exception){
        Error error = new Error("flight-already-exists", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(FlightCanNotChangeAirplaneTypeException.class)
    public ResponseEntity<Error> handleException(FlightCanNotChangeAirplaneTypeException exception){
        Error error = new Error("flight-can-not-change-airplane-type", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotBeReservedException.class)
    public ResponseEntity<Error> handleException(FlightCanNotBeReservedException exception){
        Error error = new Error("flight-not-able-to-make-reservations", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OutOfFlightCheckInTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightCheckInTimeException exception){
        Error error = new Error("flight-out-of-check-in-time", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OutOfFlightBoardingTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightBoardingTimeException exception){
        Error error = new Error("flight-out-of-boarding-time", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(PassengerAlreadyExistsException exception){
        Error error = new Error("passenger-already-exists", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerDoesNotExistException.class)
    public ResponseEntity<Error> handleException(PassengerDoesNotExistException exception){
        Error error = new Error("passenger-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerHasDifferentPassportNumberException.class)
    public ResponseEntity<Error> handleException(PassengerHasDifferentPassportNumberException exception){
        Error error = new Error("passenger-has-different-passport-number", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SeatNumberAlreadyTakenException.class)
    public ResponseEntity<Error> handleException(SeatNumberAlreadyTakenException exception){
        Error error = new Error("seat-is-already-taken", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SeatNumberOutOfRangeException.class)
    public ResponseEntity<Error> handleException(SeatNumberOutOfRangeException exception){
        Error error = new Error("seat-number-out-of-range", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DuplicateSeatNumberInReservationException.class)
    public ResponseEntity<Error> handleException(DuplicateSeatNumberInReservationException exception){
        Error error = new Error("seat-duplicated-in-request", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationMustHavePassengersException.class)
    public ResponseEntity<Error> handleException(ReservationMustHavePassengersException exception){
        Error error = new Error("reservation-with-no-passengers", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ReservationDoesNotExistException.class)
    public ResponseEntity<Error> handleException(ReservationDoesNotExistException exception){
        Error error = new Error("reservation-does-not-exist", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(PassengerNotFoundInReservationException.class)
    public ResponseEntity<Error> handleException(PassengerNotFoundInReservationException exception){
        Error error = new Error("passenger-not-found-in-reservation", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidCheckInPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidCheckInPassengerReservationException exception){
        Error error = new Error("invalid-status-for-check-in", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidBoardingPassengerReservationException.class)
    public ResponseEntity<Error> handleException(InvalidBoardingPassengerReservationException exception){
        Error error = new Error("invalid-status-for-boarding", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
