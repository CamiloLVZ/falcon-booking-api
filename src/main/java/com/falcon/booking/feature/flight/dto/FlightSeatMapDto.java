package com.falcon.booking.feature.flight.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Complete seat map for a flight, including layout configuration, current prices, and per-seat status")
public record FlightSeatMapDto(

        @Schema(description = "Seat column letters defining the aircraft layout (e.g. 'ABCDEF')", example = "ABCDEF")
        String seatColumns,

        @Schema(description = "Number of first class rows", example = "4")
        Integer firstClassRows,

        @Schema(description = "Number of economy class rows", example = "25")
        Integer economyRows,

        @Schema(description = "Current dynamic price for economy class seats", example = "350.00")
        BigDecimal priceEconomy,

        @Schema(description = "Current dynamic price for first class seats", example = "1200.00")
        BigDecimal priceFirstClass,

        @Schema(description = "Full list of seats with their individual status and price")
        List<FlightSeatDto> seats
) {}
