package com.falcon.booking.feature.route.dto;

import com.falcon.booking.common.enums.RouteStatus;
import com.falcon.booking.feature.airplaneType.dto.ResponseAirplaneTypeDto;
import com.falcon.booking.feature.airport.dto.AirportDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

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
        int durationMinutes,
        @Schema(description = "Current route status", example = "ACTIVE")
        RouteStatus status,
        @Schema(description = "Base price for economy class", example = "100.00")
        BigDecimal basePriceEconomy,
        @Schema(description = "Base price for first class", example = "200.00")
        BigDecimal basePriceFirstClass
) {
}