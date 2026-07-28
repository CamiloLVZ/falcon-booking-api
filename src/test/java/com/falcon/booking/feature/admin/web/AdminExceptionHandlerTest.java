package com.falcon.booking.feature.admin.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminExceptionHandlerTest {

    private final AdminExceptionHandler handler = new AdminExceptionHandler();

    @Test
    void shouldHandleIllegalArgumentException() {
        var response = handler.handleException(new IllegalArgumentException("Invalid argument"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().type()).isEqualTo("invalid-arguments");
    }
}
