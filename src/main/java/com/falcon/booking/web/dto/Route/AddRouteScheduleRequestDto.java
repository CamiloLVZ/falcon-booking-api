package com.falcon.booking.web.dto.Route;

import java.time.LocalTime;
import java.util.List;

public record AddRouteScheduleRequestDto(List<LocalTime> schedules) {}
