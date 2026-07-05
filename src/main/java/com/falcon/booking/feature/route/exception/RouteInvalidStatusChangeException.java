package com.falcon.booking.feature.route.exception;

import com.falcon.booking.common.enums.RouteStatus;

public class RouteInvalidStatusChangeException extends RuntimeException {
    public RouteInvalidStatusChangeException(RouteStatus initialStatus, RouteStatus finalStatus) {
        super("There is no posible change route status from "+initialStatus+" to "+finalStatus);
    }
}
