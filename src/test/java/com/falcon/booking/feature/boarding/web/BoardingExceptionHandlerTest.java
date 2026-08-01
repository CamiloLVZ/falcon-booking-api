package com.falcon.booking.feature.boarding.web;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.feature.boarding.exception.BoardingPassAlreadyBoardedException;
import com.falcon.booking.feature.boarding.exception.BoardingPassExpiredException;
import com.falcon.booking.feature.boarding.exception.BoardingPassNotFoundException;
import com.falcon.booking.feature.boarding.exception.InvalidBoardingPassengerReservationException;
import com.falcon.booking.feature.boarding.pdf.exception.PdfGenerationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BoardingExceptionHandlerTest {

    private final BoardingExceptionHandler handler = new BoardingExceptionHandler();

    @Test
    void shouldHandleBoardingPassNotFound() {
        var response = handler.handleException(new BoardingPassNotFoundException(UUID.randomUUID()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("boarding-pass-not-found");
    }

    @Test
    void shouldHandleBoardingPassNotFoundByPassengerReservationId() {
        var response = handler.handleException(new BoardingPassNotFoundException(10L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("boarding-pass-not-found");
        assertThat(response.getBody().message()).contains("10");
    }

    @Test
    void shouldHandleBoardingPassAlreadyBoarded() {
        var response = handler.handleException(new BoardingPassAlreadyBoardedException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("boarding-pass-already-boarded");
    }

    @Test
    void shouldHandleBoardingPassExpired() {
        var response = handler.handleException(new BoardingPassExpiredException("test"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("boarding-pass-expired");
    }

    @Test
    void shouldHandleInvalidBoardingPassengerReservation() {
        var response = handler.handleException(
                new InvalidBoardingPassengerReservationException(PassengerReservationStatus.RESERVED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-boarding-passenger-reservation");
    }

    @Test
    void shouldHandlePdfGenerationException() {
        var response = handler.handleException(new PdfGenerationException("test", new RuntimeException()));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().type()).isEqualTo("pdf-generation-error");
    }
}
