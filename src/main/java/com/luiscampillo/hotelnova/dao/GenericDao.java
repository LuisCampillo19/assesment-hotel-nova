package com.luiscampillo.hotelnova.dao;

import java.util.List;
import java.util.Optional;

/**
 * Base CRUD contract every DAO must satisfy.
 *   T  = entity type   (User, Guest, Room, Reservation)
 *   ID = primary-key type (Integer for all current entities)
 */
public interface GenericDao<T, ID> {
    T            save(T entity);
    Optional<T>  findById(ID id);
    List<T>      findAll();
    boolean      update(T entity);
    boolean      deleteById(ID id);
}
