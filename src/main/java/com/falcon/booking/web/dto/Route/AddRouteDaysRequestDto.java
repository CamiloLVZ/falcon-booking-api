package com.falcon.booking.web.dto.Route;

import com.falcon.booking.domain.valueobject.WeekDay;

import java.util.List;

public record AddRouteDaysRequestDto (List<WeekDay> weekDays) { }
