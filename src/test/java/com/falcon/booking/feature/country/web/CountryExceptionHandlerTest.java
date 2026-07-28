package com.falcon.booking.feature.country.web;

import com.falcon.booking.feature.country.exception.CountryAlreadyExistsException;
import com.falcon.booking.feature.country.exception.CountryNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class CountryExceptionHandlerTest {

    private final CountryExceptionHandler handler = new CountryExceptionHandler();

    @Test
    void shouldHandleCountryNotFound() {
        var response = handler.handleException(new CountryNotFoundException("CO"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().type()).isEqualTo("country-does-not-exist");
    }

    @Test
    void shouldHandleCountryAlreadyExists() {
        var response = handler.handleException(new CountryAlreadyExistsException("isoCode", "CO"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().type()).isEqualTo("country-already-exists");
    }
}
