package com.falcon.booking.feature.route.exception;

import java.time.DayOfWeek;
import java.util.Arrays;

public class RouteWeekDayInvalidException extends RuntimeException {
    public RouteWeekDayInvalidException(String day) {
        super("Value: " + day + " is not a valid week day. Valid values are: "+ Arrays.toString(DayOfWeek.values()));
    }
}
