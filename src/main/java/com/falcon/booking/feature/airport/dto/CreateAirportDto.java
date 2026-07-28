package com.falcon.booking.feature.airport.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new airport")
public record CreateAirportDto(
        @Schema(description = "Unique three character airport IATA code", example = "CTG")
        @NotBlank(message = "IATA code can not be blank")
        @Size(min = 3, max = 3, message = "IATA code must be exactly 3 characters")
        String iataCode,
        @Schema(description = "Common airport name", example = "Aeropuerto Internacional Rafael Núñez")
        @NotBlank(message = "Airport name can not be blank")
        @Size(max = 150, message = "Airport name must be at most 150 characters")
        String name,
        @Schema(description = "City where the airport is located", example = "Cartagena")
        @NotBlank(message = "City can not be blank")
        @Size(max = 150, message = "City must be at most 150 characters")
        String city,
        @Schema(description = "Country two character ISO code", example = "CO")
        @NotBlank(message = "Country ISO code can not be blank")
        @Size(min = 2, max = 2, message = "Country ISO code must be exactly 2 characters")
        String countryIsoCode,
        @Schema(description = "Time zone ID where the airport is located", example = "America/Bogota")
        @NotBlank(message = "Timezone can not be blank")
        @Size(max = 20, message = "Timezone must be at most 20 characters")
        String timezone
) {}
