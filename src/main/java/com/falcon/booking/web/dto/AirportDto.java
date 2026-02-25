package com.falcon.booking.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AirportDto(
        @Schema(description = "Unique three character airport identification by IATA", example = "BOG")
        String iataCode,
        @Schema(description = "Common airport name", example = "Aeropuerto Internacional El Dorado")
        String name,
        @Schema(description = "City where the airport is located", example = "Bogota")
        String city,
        @Schema(description = "Country where the airport is located")
        CountryDto country,
        @Schema(description = "Time zone ID where the airport is located", example = "America/Bogota")
        String timezone)
{ }
