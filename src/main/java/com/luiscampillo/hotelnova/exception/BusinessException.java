package com.luiscampillo.hotelnova.exception;

/**
 * Base class for every domain rule violation in HotelNova.
 *
 * Extends RuntimeException so that service methods do not have to declare
 * "throws" everywhere; the controller layer catches BusinessException at
 * the boundary and translates it to a user-friendly message in the view.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
