package com.falcon.booking.web.dto.route;

import java.time.DayOfWeek;
import java.util.List;

public record AddRouteDaysRequestDto (List<DayOfWeek> weekDays) { }
