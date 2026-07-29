package com.falcon.booking.feature.airplaneType.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AirplaneTypeOptionDto(
        @Schema(description = "Airplane type numeric unique identifier", example = "10")
        Long id,
        @Schema(description = "Airplane type producers name", example = "Boeing")
        String producer,
        @Schema(description = "Airplane type model name", example = "737")
        String model
) {}