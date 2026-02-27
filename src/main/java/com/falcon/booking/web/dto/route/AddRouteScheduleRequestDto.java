package com.falcon.booking.web.dto.route;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record AddRouteScheduleRequestDto(
        @ArraySchema(schema = @Schema(type = "LocalTime", example = "06:30:00"),
                arraySchema = @Schema(description = "Local departure times for the route"))
        Set<LocalTime> schedules,
        @ArraySchema(schema = @Schema(type = "String", example = "MONDAY"),
                arraySchema = @Schema(description = "Operation week days for the route"))
        Set<DayOfWeek> weekDays) {}