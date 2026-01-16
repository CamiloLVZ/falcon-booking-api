package com.falcon.booking.domain.exception.Route;

import com.falcon.booking.domain.valueobject.RouteStatus;

public class RouteInvalidStatusChangeException extends RuntimeException {
    public RouteInvalidStatusChangeException(RouteStatus initialStatus, RouteStatus finalStatus) {
        super("There is no posible change route status from "+initialStatus+" to "+finalStatus);
    }
}
