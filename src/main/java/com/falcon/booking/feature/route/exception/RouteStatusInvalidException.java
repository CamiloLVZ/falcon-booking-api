package com.falcon.booking.feature.route.exception;

import com.falcon.booking.common.enums.RouteStatus;

import java.util.Arrays;

public class RouteStatusInvalidException extends RuntimeException {
    public RouteStatusInvalidException(String status) {
        super("The value: " + status + " is not a valid route status. Valid values are: "+ Arrays.toString(RouteStatus.values()));
    }
}
