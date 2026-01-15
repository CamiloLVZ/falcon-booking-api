package com.falcon.booking.domain.valueobject;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeStatusInvalidException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AirplaneTypeStatus {
    ACTIVE,
    INACTIVE,
    RETIRED;
}