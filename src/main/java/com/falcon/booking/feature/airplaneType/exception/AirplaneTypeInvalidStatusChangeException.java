package com.falcon.booking.feature.airplaneType.exception;

import com.falcon.booking.common.enums.AirplaneTypeStatus;

public class AirplaneTypeInvalidStatusChangeException extends RuntimeException {
    public AirplaneTypeInvalidStatusChangeException(AirplaneTypeStatus initialStatus, AirplaneTypeStatus finalStatus) {
        super("There is no allowed change Airplane Type status from "+initialStatus+" to "+finalStatus);
    }
}
