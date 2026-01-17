package com.falcon.booking.web.dto.route;

import com.falcon.booking.domain.valueobject.WeekDay;

import java.time.LocalTime;
import java.util.List;

public record RouteWithSchedulesDto(String flightNumber, List<WeekDay> weekDays, List<LocalTime> schedules){ }
