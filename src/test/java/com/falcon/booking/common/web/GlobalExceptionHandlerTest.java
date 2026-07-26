package com.falcon.booking.common.web;

import com.falcon.booking.common.exception.DateToBeforeDateFromException;
import com.falcon.booking.common.exception.InvalidSearchCriteriaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleMethodArgumentNotValid() {
        BindingResult bindingResult = mock(BindingResult.class);
        given(bindingResult.getFieldErrors()).willReturn(List.of(
                new FieldError("obj", "field1", "error1"),
                new FieldError("obj", "field2", "error2")
        ));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        given(exception.getBindingResult()).willReturn(bindingResult);

        ResponseEntity<Error> response = handler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-arguments");
        assertThat(response.getBody().message()).contains("field1: error1 | field2: error2");
    }

    @Test
    void shouldHandleMissingServletRequestParameter() {
        var exception = new MissingServletRequestParameterException("param", "String");
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("required-parameter-not-found");
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatch() {
        var exception = mock(MethodArgumentTypeMismatchException.class);
        given(exception.getMessage()).willReturn("type mismatch");
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-argument-type");
    }

    @Test
    void shouldHandleConstraintViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        given(violation.getMessage()).willReturn("must be positive");
        ConstraintViolationException exception = new ConstraintViolationException("test", Set.of(violation));

        ResponseEntity<Error> response = handler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-arguments");
        assertThat(response.getBody().message()).isEqualTo("must be positive");
    }

    @Test
    void shouldHandleInvalidSearchCriteria() {
        var exception = new InvalidSearchCriteriaException("test");
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-search-criteria");
    }

    @Test
    void shouldHandleHttpMessageNotReadable() {
        var exception = new HttpMessageNotReadableException("test");
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("data-format-invalid");
    }

    @Test
    void shouldHandleDateToBeforeDateFrom() {
        var exception = new DateToBeforeDateFromException();
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("date-to-before-date-from");
    }

    @Test
    void shouldHandleNoHandlerFound() {
        var exception = new NoHandlerFoundException("GET", "/test", null);
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("endpoint-not-found");
    }

    @Test
    void shouldHandleHttpRequestMethodNotSupported() {
        var exception = new HttpRequestMethodNotSupportedException("POST", List.of("GET"));
        ResponseEntity<Error> response = handler.handleException(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().type()).isEqualTo("method-not-supported");
    }
}
