package com.luiscampillo.hotelnova.model.enums;

/** Operational status of a room. Updated transactionally on check-in / check-out. */
public enum RoomStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE
}
