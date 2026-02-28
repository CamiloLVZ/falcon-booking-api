package com.falcon.booking.web.dto.flight;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ResponseFlightsGeneratedDto(
        @Schema(description = "Route flight number", example = "AV1234")
        String flightNumber,
        @Schema(description = "Total number of generated flights", example = "30")
        Integer flightsGenerated,
        @Schema(description = "Start date of generation window", example = "2026-02-01")
        LocalDate from,
        @Schema(description = "End date of generation window", example = "2026-02-28")
        LocalDate to
) {
}