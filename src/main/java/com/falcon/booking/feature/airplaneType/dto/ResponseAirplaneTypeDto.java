package com.falcon.booking.feature.airplaneType.dto;

import com.falcon.booking.common.enums.AirplaneTypeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponseAirplaneTypeDto(
        @Schema(description = "Airplane type numeric unique identifier", example = "10")
        Long id,
        @Schema(description = "Airplane type producers name", example = "AIRBUS")
        String producer,
        @Schema(description = "Airplane type model name", example = "320-200")
        String model,
        @Schema(description = "Number of economy seats", example = "150")
        Integer economySeats,
        @Schema(description = "Number of first class seats", example = "20")
        Integer firstClassSeats,
        @Schema(description = "Seat column letters (e.g. 'ABCDEF')", example = "ABCDEF")
        String seatColumns,
        @Schema(description = "Airplane type status", example = "ACTIVE")
        AirplaneTypeStatus status
) { }
