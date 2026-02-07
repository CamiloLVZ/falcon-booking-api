package com.falcon.booking.web.dto.airplaneType;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAirplaneTypeDto(

        @Positive(message = "the value for economySeats must be a positive number")
        Integer economySeats,

        @PositiveOrZero(message = "the value for firstClassSeats must be zero or greater.")
        Integer firstClassSeats
) { }
