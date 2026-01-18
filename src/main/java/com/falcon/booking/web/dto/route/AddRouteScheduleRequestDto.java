package com.falcon.booking.web.dto.route;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

public record AddRouteScheduleRequestDto(Set<LocalTime> schedules,
                                         Set<DayOfWeek> weekDays) {}
