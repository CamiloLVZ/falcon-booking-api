package com.falcon.booking.web.dto.flight;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateFlightDto(
                                @NotBlank(message = "Flight number can not be blank")
                                @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
                                String routeFlightNumber,

                                @NotNull(message = "departureDateTime is required")
                                @Future(message = "departureDateTime must be future")
                                OffsetDateTime departureDateTime) {
}
