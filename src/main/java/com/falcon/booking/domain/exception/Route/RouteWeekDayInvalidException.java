package com.falcon.booking.domain.exception.Route;

import java.time.DayOfWeek;
import java.util.Arrays;

public class RouteWeekDayInvalidException extends RuntimeException {
    public RouteWeekDayInvalidException(String day) {
        super("The value: " + day + " is not a valid week day. Valid values are: "+ Arrays.toString(DayOfWeek.values()));
    }
}
