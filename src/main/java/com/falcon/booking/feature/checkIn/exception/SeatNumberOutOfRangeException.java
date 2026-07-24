package com.falcon.booking.feature.checkIn.exception;

public class SeatNumberOutOfRangeException extends RuntimeException {
    public SeatNumberOutOfRangeException(int seatNumber, int minimumSeatNumber, int maximunSeatNumber) {

        super("Seat number " + seatNumber + " is out of range, must be a value between "+ minimumSeatNumber+" and " + maximunSeatNumber);
    }
}
