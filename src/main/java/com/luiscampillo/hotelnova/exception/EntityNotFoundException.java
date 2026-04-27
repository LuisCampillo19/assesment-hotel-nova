package com.luiscampillo.hotelnova.exception;

/** Thrown when a lookup by id or natural key returns no row. */
public class EntityNotFoundException extends BusinessException {
    public EntityNotFoundException(String entity, Object key) {
        super(entity + " not found: " + key);
    }
}
