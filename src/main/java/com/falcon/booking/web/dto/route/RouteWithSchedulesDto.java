package com.falcon.booking.web.dto.route;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;

public record RouteWithSchedulesDto(String flightNumber, Collection<DayOfWeek> weekDays, Collection<LocalTime> schedules){ }
