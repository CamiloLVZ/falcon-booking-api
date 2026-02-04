package com.falcon.booking.web.exception;

import com.falcon.booking.domain.exception.*;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeAlreadyExistsException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneNotFoundException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeInvalidStatusChangeException;
import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeStatusInvalidException;
import com.falcon.booking.domain.exception.Flight.*;
import com.falcon.booking.domain.exception.Passenger.PassengerAlreadyExistsException;
import com.falcon.booking.domain.exception.Passenger.PassengerNotFoundException;
import com.falcon.booking.domain.exception.Passenger.PassengerHasDifferentPassportNumberException;
import com.falcon.booking.domain.exception.Reservation.*;
import com.falcon.booking.domain.exception.Route.*;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<Error>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        List<Error> errors = new ArrayList<>();
        StringBuilder detailsBuilder = new StringBuilder();

        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errors.add(new Error(fieldError.getField(), fieldError.getDefaultMessage()));
            detailsBuilder.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage());

            if (i < fieldErrors.size() - 1) {
                detailsBuilder.append(" | ");
            }
        }

        logger.warn("Validation failed for request: [{}]", detailsBuilder.toString());

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Error> handleException(MissingServletRequestParameterException exception){
        Error error = new Error("required-parameter-not-found", exception.getMessage());
        logger.warn("Request with required parameters not found: {}", error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Error> handleException(MethodArgumentTypeMismatchException exception){
        Error error = new Error("invalid-argument-type", exception.getMessage());
        logger.warn("Request with invalid argument type: {}", error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Error> handleValidationExceptions(ConstraintViolationException exception) {

        String firstMessage = exception.getConstraintViolations().iterator().next().getMessage();
        Error error = new Error("invalid-arguments", firstMessage);
        logger.warn("Request with invalid arguments: {}", error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ResponseEntity<Error> handleException(InvalidSearchCriteriaException exception){
        Error error = new Error("invalid-search-criteria", exception.getMessage());
        logger.warn("Request with invalid search criteria: {}", error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<Error> handleException(CountryNotFoundException exception){
        Error error = new Error("country-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirportNotFoundException.class)
    public ResponseEntity<Error> handleException(AirportNotFoundException exception){
        Error error = new Error("airport-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirplaneNotFoundException.class)
    public ResponseEntity<Error> handleException(AirplaneNotFoundException exception){
        Error error = new Error("airplane-type-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AirplaneTypeAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeAlreadyExistsException exception){
        Error error = new Error("airplane-type-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeStatusInvalidException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeStatusInvalidException exception){
        Error error = new Error("airplane-type-status-invalid", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AirplaneTypeInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(AirplaneTypeInvalidStatusChangeException exception){
        Error error = new Error("invalid-status-change", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteAirplaneTypeIsNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteAirplaneTypeIsNotActiveException exception){
        Error error = new Error("route-airplane-type-is-not-active", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(RouteAlreadyExistsException exception){
        Error error = new Error("route-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Error> handleException(RouteNotFoundException exception){
        Error error = new Error("route-does-not-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RouteInvalidStatusChangeException.class)
    public ResponseEntity<Error> handleException(RouteInvalidStatusChangeException exception){
        Error error = new Error("route-invalid-status-change", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteSameOriginAndDestinationException.class)
    public ResponseEntity<Error> handleException(RouteSameOriginAndDestinationException exception){
        Error error = new Error("route-same-origin-and-destination", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteStatusInvalidException.class)
    public ResponseEntity<Error> handleException(RouteStatusInvalidException exception){
        Error error = new Error("route-status-invalid", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteDraftInvalidUpdateException.class)
    public ResponseEntity<Error> handleException(RouteDraftInvalidUpdateException exception){
        Error error = new Error("route-can-not-change-origin-or-destination", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteWeekDayInvalidException.class)
    public ResponseEntity<Error> handleException(RouteWeekDayInvalidException exception){
        Error error = new Error("route-week-day-invalid", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotActiveException.class)
    public ResponseEntity<Error> handleException(RouteNotActiveException exception) {
        Error error = new Error("route-not-active", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteHasNotSchedulesToGenerateFlightsException.class)
    public ResponseEntity<Error> handleException(RouteHasNotSchedulesToGenerateFlightsException exception) {
        Error error = new Error("route-has-not-schedules-for-flights", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidRouteStatusForFlightGenerationException.class)
    public ResponseEntity<Error> handleException(InvalidRouteStatusForFlightGenerationException exception) {
        Error error = new Error("invalid-route-status-to-generate-flights", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RouteNotActivableException.class)
    public ResponseEntity<Error> handleException(RouteNotActivableException exception) {
        Error error = new Error("route-is-not-activable", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Error> handleException(HttpMessageNotReadableException exception) {
        Error error = new Error("data-format-invalid", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DateToBeforeDateFromException.class)
    public ResponseEntity<Error> handleException(DateToBeforeDateFromException exception){
        Error error = new Error("date-to-before-date-from", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<Error> handleException(FlightNotFoundException exception) {
        Error error = new Error("flight-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(FlightAlreadyExistsException exception){
        Error error = new Error("flight-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotChangeAirplaneTypeException.class)
    public ResponseEntity<Error> handleException(FlightCanNotChangeAirplaneTypeException exception){
        Error error = new Error("flight-can-not-change-airplane-type", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotBeReservedException.class)
    public ResponseEntity<Error> handleException(FlightCanNotBeReservedException exception){
        Error error = new Error("flight-not-able-to-make-reservations", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FlightCanNotBeRescheduledException.class)
    public ResponseEntity<Error> handleException(FlightCanNotBeRescheduledException exception){
        Error error = new Error("flight-can-not-be-re-scheduled", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OutOfFlightCheckInTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightCheckInTimeException exception){
        Error error = new Error("flight-out-of-check-in-time", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OutOfFlightBoardingTimeException.class)
    public ResponseEntity<Error> handleException(OutOfFlightBoardingTimeException exception){
        Error error = new Error("flight-out-of-boarding-time", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerAlreadyExistsException.class)
    public ResponseEntity<Error> handleException(PassengerAlreadyExistsException exception){
        Error error = new Error("passenger-already-exists", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<Error> handleException(PassengerNotFoundException exception){
        Error error = new Error("passenger-does-not-exist", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PassengerHasDifferentPassportNumberException.class)
    public ResponseEntity<Error> handleException(PassengerHasDifferentPassportNumberException exception){
        Error error = new Error("passenger-has-different-passport-number", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

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
