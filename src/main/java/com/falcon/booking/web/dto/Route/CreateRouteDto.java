package com.falcon.booking.web.dto.Route;

import com.falcon.booking.domain.common.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRouteDto(
        @NotBlank(message = "Flight number can not be blank")
        @Size(min = 5, max = 7, message = "Flight number must be an alphanumeric value with 5 to 7 characters")
        String flightNumber,

        @NotBlank(message = "Airport origin IATA code can not be null")
        @Size(min = 3, max = 3, message = "Airport Origin IATA code must be 3 letter")
        String airportOriginIataCode,

        @NotBlank(message = "Airport destination IATA code can not be null")
        @Size(min = 3, max = 3, message = "Airport Destination IATA code must be 3 letter")
        String airportDestinationIataCode,

        @NotNull(message = "Default airplane type id can not be null")
        @Positive(message = "Default airplane type id must be an integer greater than zero")
        Long idDefaultAirplaneType,

        @NotNull(message = "length minutes can not be null")
        @Positive(message = "length minutes must be a integer greater than zero")
        Integer lengthMinutes
) {

        public CreateRouteDto{
                flightNumber = StringNormalizer.normalize(flightNumber);
                airportOriginIataCode= StringNormalizer.normalize(airportOriginIataCode);
                airportDestinationIataCode =StringNormalizer.normalize(airportDestinationIataCode);
        }


}
