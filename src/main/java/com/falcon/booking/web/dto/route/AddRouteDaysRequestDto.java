package com.falcon.booking.web.dto.route;

import com.falcon.booking.domain.valueobject.WeekDay;

import java.util.List;

public record AddRouteDaysRequestDto (List<WeekDay> weekDays) { }
