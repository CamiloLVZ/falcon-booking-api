package com.falcon.booking.web.dto.Route;

import jakarta.validation.constraints.Positive;

public record UpdateRouteDto(

        @Positive(message = "Default airplane type id must be an integer greater than zero")
        Long idDefaultAirplaneType,

        @Positive(message = "length minutesmust be a integer greater than zero")
        Integer lengthMinutes
) {
}
