package com.falcon.booking.feature.checkIn.web;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.web.Error;
import com.falcon.booking.feature.checkIn.exception.InvalidCheckInPassengerReservationStatusException;
import com.falcon.booking.feature.checkIn.exception.SeatNumberAlreadyTakenException;
import com.falcon.booking.feature.checkIn.exception.SeatNumberOutOfRangeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CheckInExceptionHandlerTest {

    private final CheckInExceptionHandler handler = new CheckInExceptionHandler();

    @Test
    void shouldHandleSeatNumberAlreadyTaken() {
        var response = handler.handleException(new SeatNumberAlreadyTakenException(1, 1L));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("seat-already-taken");
    }

    @Test
    void shouldHandleInvalidCheckInPassengerReservationStatus() {
        var response = handler.handleException(
                new InvalidCheckInPassengerReservationStatusException(PassengerReservationStatus.RESERVED));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-check-in-reservation-status");
    }

    @Test
    void shouldHandleSeatNumberOutOfRange() {
        var response = handler.handleException(new SeatNumberOutOfRangeException(200, 1, 100));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("seat-number-out-of-range");
    }
}
