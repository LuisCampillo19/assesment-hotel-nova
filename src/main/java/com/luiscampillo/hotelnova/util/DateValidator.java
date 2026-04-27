package com.luiscampillo.hotelnova.util;

import com.luiscampillo.hotelnova.exception.InvalidDateRangeException;

import java.time.LocalDate;

/**
 * Validates reservation date ranges. Centralised so the same rule applies
 * everywhere a date pair is accepted (services, controllers, tests).
 */
public final class DateValidator {

    private DateValidator() { }

    /**
     * Throws InvalidDateRangeException if any of the following holds:
     *   - either date is null
     *   - check-in is on or after check-out
     *   - check-in is in the past (before today)
     */
    public static void validateReservationDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidDateRangeException("check-in and check-out must not be null");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new InvalidDateRangeException(
                    "check-in (" + checkIn + ") must be strictly before check-out (" + checkOut + ")");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException(
                    "check-in date cannot be in the past: " + checkIn);
        }
    }
}
