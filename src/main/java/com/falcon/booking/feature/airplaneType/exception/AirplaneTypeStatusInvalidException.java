package com.falcon.booking.feature.airplaneType.exception;

import com.falcon.booking.common.enums.AirplaneTypeStatus;

import java.util.Arrays;

public class AirplaneTypeStatusInvalidException extends RuntimeException {
    public AirplaneTypeStatusInvalidException(String status) {

        super("Invalid status value: " + status + ". Valid values are: "+ Arrays.toString(AirplaneTypeStatus.values()));
    }
}
