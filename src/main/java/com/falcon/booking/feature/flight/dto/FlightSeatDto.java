package com.falcon.booking.feature.flight.dto;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.common.enums.SeatStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Seat information for a specific flight")
public record FlightSeatDto(

        @Schema(description = "Internal seat number (sequential from 1 to total seats)", example = "12")
        Integer number,

        @Schema(description = "Human-readable seat label combining row and column", example = "2C")
        String label,

        @Schema(description = "Seat class category", example = "ECONOMY")
        SeatClass seatClass,

        @Schema(description = "Current availability status of the seat", example = "AVAILABLE")
        SeatStatus status,

        @Schema(description = "Current dynamic price for this seat based on occupancy and proximity to departure", example = "350.00")
        BigDecimal price
) {}
