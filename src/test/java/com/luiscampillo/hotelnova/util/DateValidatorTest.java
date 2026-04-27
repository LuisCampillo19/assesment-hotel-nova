package com.luiscampillo.hotelnova.util;

import com.luiscampillo.hotelnova.exception.InvalidDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Covers business rule R4: valid reservation dates. */
class DateValidatorTest {

    @Test
    void acceptsFutureValidRange() {
        LocalDate in  = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(3);
        assertDoesNotThrow(() -> DateValidator.validateReservationDates(in, out));
    }

    @Test
    void rejectsCheckInEqualToCheckOut() {
        LocalDate d = LocalDate.now().plusDays(1);
        assertThrows(InvalidDateRangeException.class,
                () -> DateValidator.validateReservationDates(d, d));
    }

    @Test
    void rejectsCheckInAfterCheckOut() {
        LocalDate in  = LocalDate.now().plusDays(5);
        LocalDate out = LocalDate.now().plusDays(2);
        assertThrows(InvalidDateRangeException.class,
                () -> DateValidator.validateReservationDates(in, out));
    }

    @Test
    void rejectsPastCheckIn() {
        LocalDate in  = LocalDate.now().minusDays(1);
        LocalDate out = LocalDate.now().plusDays(2);
        assertThrows(InvalidDateRangeException.class,
                () -> DateValidator.validateReservationDates(in, out));
    }

    @Test
    void rejectsNullDates() {
        assertThrows(InvalidDateRangeException.class,
                () -> DateValidator.validateReservationDates(null, null));
    }
}
