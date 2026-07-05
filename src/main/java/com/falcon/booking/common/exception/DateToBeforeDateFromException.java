package com.falcon.booking.common.exception;

public class DateToBeforeDateFromException extends RuntimeException {

    public DateToBeforeDateFromException() {
        super("Dates not valid, 'to' is before 'from' ");
    }
}
