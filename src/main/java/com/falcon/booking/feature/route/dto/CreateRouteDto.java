package com.falcon.booking.feature.route.dto;

import com.falcon.booking.common.utils.StringNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateRouteDto(
        @Schema(description = "Route unique flight number", example = "AV1234")
        @NotBlank(message = "Flight number can not be blank")
        @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
        String flightNumber,

        @Schema(description = "Origin airport IATA code", example = "BOG")
        @NotBlank(message = "Airport origin IATA code can not be null")
        @Size(min = 3, max = 3, message = "Airport Origin IATA code must be 3 letter")
        String airportOriginIataCode,

        @Schema(description = "Destination airport IATA code", example = "MIA")
        @NotBlank(message = "Airport destination IATA code can not be null")
        @Size(min = 3, max = 3, message = "Airport Destination IATA code must be 3 letter")
        String airportDestinationIataCode,

        @Schema(description = "Default airplane type numeric identifier", example = "10")
        @NotNull(message = "Default airplane type id can not be null")
        @Positive(message = "Default airplane type id must be an integer greater than zero")
        Long idDefaultAirplaneType,

        @Schema(description = "Route duration in minutes", example = "180")
        @NotNull(message = "duration minutes can not be null")
        @Positive(message = "duration minutes must be a integer greater than zero")
        Integer durationMinutes,

        @Schema(description = "Base price for economy class", example = "100.00")
        @NotNull(message = "Base price economy can not be null")
        @PositiveOrZero(message = "Base price economy must be positive or zero")
        BigDecimal basePriceEconomy,

        @Schema(description = "Base price for first class", example = "200.00")
        @NotNull(message = "Base price first class can not be null")
        @PositiveOrZero(message = "Base price first class must be positive or zero")
        BigDecimal basePriceFirstClass
) {

        public CreateRouteDto{
                flightNumber = StringNormalizer.normalize(flightNumber);
                airportOriginIataCode= StringNormalizer.normalize(airportOriginIataCode);
                airportDestinationIataCode =StringNormalizer.normalize(airportDestinationIataCode);
        }


}