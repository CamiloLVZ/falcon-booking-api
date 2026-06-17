package com.falcon.booking.feature.route.exception;

import java.time.DayOfWeek;
import java.util.Arrays;

public class RouteDayOfWeekInvalidException extends RuntimeException {
    public RouteDayOfWeekInvalidException(String day) {
        super("Value: " + day + " is not a valid week day. Valid values are: "+ Arrays.toString(DayOfWeek.values()));
    }
}
