package com.falcon.booking.domain.exception;

public class DateToBeforeDateFromException extends RuntimeException {

    public DateToBeforeDateFromException() {
        super("Dates are not valid, 'to' can not be before 'from' ");
    }
}
