package com.falcon.booking.domain.exception.Reservation;

public class SeatNumberOutOfRangeException extends RuntimeException {
    public SeatNumberOutOfRangeException(int seatNumber, int maximunSeatNumber) {

        super("Seat number " + seatNumber + " is out of range, must be a value between 1 and " + maximunSeatNumber);
    }
}
