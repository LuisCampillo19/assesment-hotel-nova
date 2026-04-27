package com.luiscampillo.hotelnova.exception;

/** Thrown when the requested room is not in AVAILABLE state. */
public class RoomNotAvailableException extends BusinessException {
    public RoomNotAvailableException(String roomNumber) {
        super("Room is not available: " + roomNumber);
    }
}
