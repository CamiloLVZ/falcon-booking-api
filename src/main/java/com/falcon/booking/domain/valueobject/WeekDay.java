package com.falcon.booking.domain.valueobject;

import com.falcon.booking.domain.exception.Route.RouteWeekDayInvalidException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum WeekDay {

    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    @JsonCreator
    public static WeekDay fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new RouteWeekDayInvalidException(value);
        }
        try {
            return WeekDay.valueOf(value.toUpperCase().trim());
        } catch (Exception e) {
            throw new RouteWeekDayInvalidException(value);
        }
    }

}
