package com.falcon.booking.web.dto.airplaneType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAirplaneTypeDto(

        @Schema(description = "Integer quantity of the new total economy seats in the airplane type", example = "150")
        @Positive(message = "the value for economySeats must be a positive number")
        Integer economySeats,

        @Schema(description = "Integer quantity of the new total first class seats in the airplane type", example = "20")
        @PositiveOrZero(message = "the value for firstClassSeats must be zero or greater.")
        Integer firstClassSeats
) { }
