package com.falcon.booking.domain.exception;

public class AirplaneTypeStatusInvalidException extends RuntimeException {
    public AirplaneTypeStatusInvalidException(String status) {

        super("The value: " + status + " is not a status for an airplane type.");
    }
}
