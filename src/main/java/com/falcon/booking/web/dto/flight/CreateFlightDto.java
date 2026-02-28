package com.falcon.booking.web.dto.flight;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateFlightDto(
        @Schema(description = "Route flight number associated to this flight", example = "AV1234")
        @NotBlank(message = "Flight number can not be blank")
        @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
        String routeFlightNumber,

        @Schema(description = "Local departure date and time for the flight", example = "2026-02-20T14:30:00")
        @NotNull(message = "departureDateTime is required")
        @Future(message = "departureDateTime must be future")
        LocalDateTime departureDateTime) {
}