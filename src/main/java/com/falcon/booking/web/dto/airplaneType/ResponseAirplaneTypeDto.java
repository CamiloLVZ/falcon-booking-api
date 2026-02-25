package com.falcon.booking.web.dto.airplaneType;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponseAirplaneTypeDto(
        @Schema(description = "Airplane type numeric unique identifier ", example = "10")
        Long id,
        @Schema(description = "Airplane type producers name", example = "AIRBUS")
        String producer,
        @Schema(description = "Airplane type model name", example = "320-200")
        String model,
        @Schema(description = "Integer quantity of economy seats in the airplane type", example = "150")
        Integer economySeats,
        @Schema(description = "Integer quantity of first class seats in the airplane type", example = "20")
        Integer firstClassSeats,
        @Schema(description = "Airplane type status", example = "ACTIVE")
        AirplaneTypeStatus status
) { }
