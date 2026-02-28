package com.falcon.booking.web.dto.route;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.web.dto.airplaneType.ResponseAirplaneTypeDto;
import com.falcon.booking.web.dto.AirportDto;
import io.swagger.v3.oas.annotations.media.Schema;

public record ResponseRouteDto(
        @Schema(description = "Route unique flight number", example = "AV1234")
        String flightNumber,
        @Schema(description = "Origin airport data")
        AirportDto airportOrigin,
        @Schema(description = "Destination airport data")
        AirportDto airportDestination,
        @Schema(description = "Default airplane type for route")
        ResponseAirplaneTypeDto defaultAirplaneType,
        @Schema(description = "Route duration in minutes", example = "180")
        int lengthMinutes,
        @Schema(description = "Current route status", example = "ACTIVE")
        RouteStatus status
) {
}