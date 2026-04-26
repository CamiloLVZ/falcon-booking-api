package com.falcon.booking.web.dto.flight;

import com.falcon.booking.domain.valueobject.FlightStatus;
import com.falcon.booking.web.dto.airplaneType.AirplaneTypeInFlightDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ResponseFlightDto(
        @Schema(description = "Flight numeric unique identifier", example = "100")
        Long id,
        @Schema(description = "Route flight number", example = "AV1234")
        String flightNumber,
        @Schema(description = "Origin airport IATA code", example = "BOG")
        String origin,
        @Schema(description = "Destination airport IATA code", example = "MIA")
        String destination,
        @Schema(description = "Flight departure date time in UTC timezone", example = "2026-02-20T19:30:00Z")
        OffsetDateTime departureDateTime,
        @Schema(description = "Flight local departure date time", example = "2026-02-20T14:30:00")
        LocalDateTime localDepartureDateTime,
        @Schema(description = "Flight duration in minutes")
        int durationMinutes,
        @Schema(description = "Assigned airplane type information")
        AirplaneTypeInFlightDto airplaneType,
        @Schema(description = "Current flight status", example = "SCHEDULED")
        FlightStatus status) {
}