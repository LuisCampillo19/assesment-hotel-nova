package com.luiscampillo.hotelnova.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Covers business rule R7: total cost = nights * pricePerNight * (1 + IVA). */
class CostCalculatorTest {

    @Test
    void computesCostFor3NightsAt100000With19PercentIva() {
        // 3 * 100000 = 300000
        // 300000 * 1.19 = 357000
        BigDecimal result = CostCalculator.compute(
                new BigDecimal("100000"), 3, new BigDecimal("0.19"));
        assertEquals(0, result.compareTo(new BigDecimal("357000.00")));
    }

    @Test
    void computesCostWithZeroIva() {
        BigDecimal result = CostCalculator.compute(
                new BigDecimal("50000"), 2, BigDecimal.ZERO);
        assertEquals(0, result.compareTo(new BigDecimal("100000.00")));
    }

    @Test
    void rejectsZeroNights() {
        assertThrows(IllegalArgumentException.class,
                () -> CostCalculator.compute(new BigDecimal("100"), 0, BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeNights() {
        assertThrows(IllegalArgumentException.class,
                () -> CostCalculator.compute(new BigDecimal("100"), -1, BigDecimal.ZERO));
    }

    @Test
    void rejectsNullPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> CostCalculator.compute(null, 1, BigDecimal.ZERO));
    }
}
