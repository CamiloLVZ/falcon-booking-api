package com.falcon.booking.feature.airport.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AirportSearchOptionDto(
        @Schema(description = "Unique three character airport identification by IATA", example = "BOG")
        String iataCode,

        @Schema(description = "City where the airport is located", example = "Bogota")
        String city,

        @Schema(description = "Common airport name", example = "Aeropuerto Internacional El Dorado")
        String name
) {}
