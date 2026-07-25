package com.falcon.booking.feature.reservation.exception;

public class ReservationCancellationTimeExpiredException extends RuntimeException {
    public ReservationCancellationTimeExpiredException(String reservationNumber, long minimumHours) {
        super("Reservation " + reservationNumber + " cannot be canceled less than " + minimumHours + " hours before flight departure");
    }
}
