package com.luiscampillo.hotelnova.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure function that computes total reservation cost.
 * Pulled into its own class so it can be unit-tested without any DB or DAO.
 *
 *   cost = nights * pricePerNight * (1 + iva)
 *
 * All arithmetic uses BigDecimal to preserve monetary precision; the result
 * is rounded HALF_UP to two decimal places.
 */
public final class CostCalculator {

    private CostCalculator() { }

    public static BigDecimal compute(BigDecimal pricePerNight, long nights, BigDecimal iva) {
        if (pricePerNight == null) {
            throw new IllegalArgumentException("pricePerNight must not be null");
        }
        if (iva == null) {
            throw new IllegalArgumentException("iva must not be null");
        }
        if (nights <= 0) {
            throw new IllegalArgumentException("nights must be greater than zero");
        }
        BigDecimal subtotal   = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal multiplier = BigDecimal.ONE.add(iva);
        return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
