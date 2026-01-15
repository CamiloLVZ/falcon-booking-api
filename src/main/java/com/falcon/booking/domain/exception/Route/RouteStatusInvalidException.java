package com.falcon.booking.domain.exception.Route;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;
import com.falcon.booking.domain.valueobject.RouteStatus;

import java.util.Arrays;

public class RouteStatusInvalidException extends RuntimeException {
    public RouteStatusInvalidException(String status) {
        super("The value: " + status + " is not a valid status. Valid values are: "+ Arrays.toString(RouteStatus.values()));
    }
}
