package com.falcon.booking.web.dto.flight;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Wrapper response for flight search results")
public record FlightSearchResponse(
        @Schema(description = "List of flights matching the search criteria")
        List<ResponseFlightDto> data,
        @Schema(description = "Total number of flights matching the search criteria", example = "25")
        int total,
        @Schema(description = "Flights departure date", example = "2026-02-20")
        LocalDate date
) {}
