package com.luiscampillo.hotelnova.exception;

/** Thrown when attempting check-out on a reservation that is not in ACTIVE status. */
public class CheckoutWithoutCheckinException extends BusinessException {
    public CheckoutWithoutCheckinException(int reservationId) {
        super("Cannot check out reservation #" + reservationId
                + " because it has no active check-in.");
    }
}
