package com.falcon.booking.feature.reservation.exception;

public class DuplicatedPassengerException extends RuntimeException {
    public DuplicatedPassengerException(String id) {
        super("The passenger with id " + id + " is duplicated in the request.");
    }
}
