package com.falcon.booking.common.web;

import com.falcon.booking.common.exception.DateToBeforeDateFromException;
import com.falcon.booking.common.exception.InvalidSearchCriteriaException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Error> handleValidationExceptions(MethodArgumentNotValidException exception) {
        StringBuilder detailsBuilder = new StringBuilder();

        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            detailsBuilder.append(fieldError.getField())
                    .append(": ")
                    .append(fieldError.getDefaultMessage());

            if (i < fieldErrors.size() - 1) {
                detailsBuilder.append(" | ");
            }
        }

        logger.warn("Validation failed for request: [{}]", detailsBuilder.toString());

        Error error = new Error("invalid-arguments", detailsBuilder.toString());
        return ResponseEntity.badRequest().body(error);
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

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Error> handleException(NoHandlerFoundException exception) {
        Error error = new Error("endpoint-not-found", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Error> handleException(HttpRequestMethodNotSupportedException exception) {
        Error error = new Error("method-not-supported", exception.getMessage());
        logger.warn(error.message());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }
}
