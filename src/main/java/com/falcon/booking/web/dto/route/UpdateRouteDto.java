package com.falcon.booking.web.dto.route;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateRouteDto(

        @Schema(description = "Origin airport IATA code", example = "BOG")
        @Size(min = 3, max = 3, message = "Airport Origin IATA code must be 3 letter")
        String airportOriginIataCode,

        @Schema(description = "Destination airport IATA code", example = "MIA")
        @Size(min = 3, max = 3, message = "Airport Destination IATA code must be 3 letter")
        String airportDestinationIataCode,

        @Schema(description = "Default airplane type numeric identifier", example = "10")
        @Positive(message = "Default airplane type id must be an integer greater than zero")
        Long idDefaultAirplaneType,

        @Schema(description = "Route duration in minutes", example = "180")
        @Positive(message = "length minutesmust be a integer greater than zero")
        Integer lengthMinutes
) {
        public UpdateRouteDto{
                airportOriginIataCode= StringNormalizer.normalize(airportOriginIataCode);
                airportDestinationIataCode = StringNormalizer.normalize(airportDestinationIataCode);
        }
}