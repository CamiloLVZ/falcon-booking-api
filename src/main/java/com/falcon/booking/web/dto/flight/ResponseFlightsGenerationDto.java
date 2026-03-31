package com.falcon.booking.web.dto.flight;

import com.falcon.booking.domain.valueobject.FlightGenerationStatus;
import com.falcon.booking.domain.valueobject.FlightGenerationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(
        name = "ResponseFlightsGenerationDto",
        description = "Represents the current state and data of a flight generation process."
)
public record ResponseFlightsGenerationDto(

        @Schema(
                description = "Unique identifier of the generation process.",
                example = "42"
        )
        Long generationId,

        @Schema(
                description = "Current status of the generation process.",
                example = "RUNNING"
        )
        FlightGenerationStatus status,

        @Schema(
                description = "Type of generation being executed.",
                example = "ROUTE"
        )
        FlightGenerationType type,

        @Schema(
                description = "Route identifier associated with the generation. Present only when type is ROUTE.",
                example = "10",
                nullable = true
        )
        Long routeId,

        @Schema(
                description = "Total number of flights generated. Only available when the process is completed.",
                example = "1250",
                nullable = true
        )
        Integer totalGenerated,

        @Schema(
                description = "Date and time when the generation started (UTC).",
                example = "2026-03-01T10:15:30"
        )
        Instant startedAt,

        @Schema(
                description = "Date and time when the generation finished (UTC). Null if still running.",
                example = "2026-03-01T10:15:48",
                nullable = true
        )
        Instant finishedAt,

        @Schema(
                description = "Total duration of the generation process in seconds. Null if not finished.",
                example = "18",
                nullable = true
        )
        Long durationSeconds,

        @Schema(
                description = "URL endpoint to query the status of this generation.",
                example = "/flight-generations/42"
        )
        String statusUrl
) {
}