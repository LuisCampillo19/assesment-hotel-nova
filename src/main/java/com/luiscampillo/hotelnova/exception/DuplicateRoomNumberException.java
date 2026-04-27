package com.luiscampillo.hotelnova.exception;

/** Thrown when registering a room whose number already exists. */
public class DuplicateRoomNumberException extends BusinessException {
    public DuplicateRoomNumberException(String roomNumber) {
        super("Room number already exists: " + roomNumber);
    }
}
