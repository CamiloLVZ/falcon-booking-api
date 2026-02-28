package com.falcon.booking.web.dto.route;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;

public record RouteWithSchedulesDto(
        @Schema(description = "Route unique flight number", example = "AV1234")
        String flightNumber,
        @ArraySchema(schema = @Schema(type = "string", example = "MONDAY"),
                arraySchema = @Schema(description = "Configured operation week days"))
        Collection<DayOfWeek> weekDays,
        @ArraySchema(schema = @Schema(type = "string", example = "06:30:00"),
                arraySchema = @Schema(description = "Configured local departure times"))
        Collection<LocalTime> schedules){ }
