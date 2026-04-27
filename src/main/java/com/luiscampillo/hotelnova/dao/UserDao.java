package com.luiscampillo.hotelnova.dao;

import com.luiscampillo.hotelnova.model.entity.User;

import java.util.Optional;

public interface UserDao extends GenericDao<User, Integer> {

    /** Looks up a user by username. Used by the login flow. */
    Optional<User> findByUsername(String username);
}
