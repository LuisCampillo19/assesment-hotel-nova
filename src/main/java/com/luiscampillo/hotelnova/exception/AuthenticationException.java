package com.luiscampillo.hotelnova.exception;

/** Thrown when login fails (wrong credentials or inactive user). */
public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super(message);
    }
}
