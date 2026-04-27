package com.luiscampillo.hotelnova.exception;

/** Thrown when trying to make a reservation for a deactivated guest. */
public class InactiveGuestException extends BusinessException {
    public InactiveGuestException(String document) {
        super("Guest is inactive and cannot make reservations: " + document);
    }
}
