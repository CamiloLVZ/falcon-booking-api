package com.falcon.booking.domain.exception.AirplaneType;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;

public class AirplaneTypeInvalidStatusChangeException extends RuntimeException {
    public AirplaneTypeInvalidStatusChangeException(AirplaneTypeStatus initialStatus, AirplaneTypeStatus finalStatus) {
        super("There is no allowed change Airplane Type status from "+initialStatus+" to "+finalStatus);
    }
}
