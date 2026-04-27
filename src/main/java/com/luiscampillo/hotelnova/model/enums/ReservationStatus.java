package com.luiscampillo.hotelnova.model.enums;

/**
 * Lifecycle of a reservation:
 *   PENDING   -> created, guest has not arrived yet
 *   ACTIVE    -> guest checked in
 *   COMPLETED -> guest checked out
 *   CANCELLED -> voided before check-in
 */
public enum ReservationStatus {
    PENDING,
    ACTIVE,
    COMPLETED,
    CANCELLED
}
