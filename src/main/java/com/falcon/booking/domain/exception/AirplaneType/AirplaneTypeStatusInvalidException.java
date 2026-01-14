package com.falcon.booking.domain.exception.AirplaneType;

import com.falcon.booking.domain.valueobject.AirplaneTypeStatus;

import java.util.Arrays;

public class AirplaneTypeStatusInvalidException extends RuntimeException {
    public AirplaneTypeStatusInvalidException(String status) {

        super("The value: " + status + " is not a valid status. Valid values are: "+ Arrays.toString(AirplaneTypeStatus.values()));
    }
}
