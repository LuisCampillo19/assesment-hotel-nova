package com.luiscampillo.hotelnova.exception;

/** Thrown when a new reservation collides with an existing one for the same room. */
public class OverlappingReservationException extends BusinessException {
    public OverlappingReservationException(String roomNumber) {
        super("Room already has a reservation in the requested date range: " + roomNumber);
    }
}
