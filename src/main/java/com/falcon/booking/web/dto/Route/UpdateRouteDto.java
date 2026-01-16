package com.falcon.booking.web.dto.Route;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateRouteDto(

        @Size(min = 3, max = 3, message = "Airport Origin IATA code must be 3 letter")
        String airportOriginIataCode,

        @Size(min = 3, max = 3, message = "Airport Destination IATA code must be 3 letter")
        String airportDestinationIataCode,

        @Positive(message = "Default airplane type id must be an integer greater than zero")
        Long idDefaultAirplaneType,

        @Positive(message = "length minutesmust be a integer greater than zero")
        Integer lengthMinutes
) {
        public UpdateRouteDto{
                airportOriginIataCode= StringNormalizer.normalize(airportOriginIataCode);
                airportDestinationIataCode = StringNormalizer.normalize(airportDestinationIataCode);
        }
}
