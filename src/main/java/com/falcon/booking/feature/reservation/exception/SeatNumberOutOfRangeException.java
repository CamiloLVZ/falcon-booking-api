package com.falcon.booking.feature.reservation.exception;

public class SeatNumberOutOfRangeException extends RuntimeException {
    public SeatNumberOutOfRangeException(int seatNumber, int minimumSeatNumber, int maximunSeatNumber) {

        super("Seat number " + seatNumber + " is out of range, must be a value between "+ minimumSeatNumber+" and " + maximunSeatNumber);
    }
}
