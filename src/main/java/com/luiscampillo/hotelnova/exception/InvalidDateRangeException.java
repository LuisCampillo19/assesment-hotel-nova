package com.luiscampillo.hotelnova.exception;

/** Thrown when check-in / check-out dates are missing, equal, or inverted. */
public class InvalidDateRangeException extends BusinessException {
    public InvalidDateRangeException(String detail) {
        super("Invalid reservation dates: " + detail);
    }
}
