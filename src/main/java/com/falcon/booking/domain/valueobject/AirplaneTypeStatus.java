package com.falcon.booking.domain.valueobject;

import com.falcon.booking.domain.exception.AirplaneType.AirplaneTypeStatusInvalidException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum AirplaneTypeStatus {
    ACTIVE,
    INACTIVE,
    RETIRED;

    @JsonCreator
    public static AirplaneTypeStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return AirplaneTypeStatus.valueOf(value.toUpperCase().trim());
        } catch (Exception e) {
            throw new AirplaneTypeStatusInvalidException(value);
        }
    }
}