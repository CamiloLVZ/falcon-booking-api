package com.falcon.booking.web.dto.route;

import java.time.LocalTime;
import java.util.List;

public record AddRouteScheduleRequestDto(List<LocalTime> schedules) {}
