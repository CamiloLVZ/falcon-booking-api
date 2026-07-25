package com.falcon.booking.feature.passenger.exception;

public class PassengerProfileAlreadyLinkedException extends RuntimeException {
    public PassengerProfileAlreadyLinkedException() {
        super("A passenger profile is already linked to this account. Use PUT to update it");
    }
}
