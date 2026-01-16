package com.falcon.booking.domain.exception.Route;

import com.falcon.booking.domain.valueobject.RouteStatus;
import com.falcon.booking.domain.valueobject.WeekDay;

import java.util.Arrays;

public class RouteWeekDayInvalidException extends RuntimeException {
    public RouteWeekDayInvalidException(String day) {
        super("The value: " + day + " is not a valid week day. Valid values are: "+ Arrays.toString(WeekDay.values()));
    }
}
