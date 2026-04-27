package com.luiscampillo.hotelnova.dao;

import com.luiscampillo.hotelnova.model.entity.Guest;

import java.util.Optional;

public interface GuestDao extends GenericDao<Guest, Integer> {

    /** Looks up a guest by their unique national document number. */
    Optional<Guest> findByDocumentNumber(String documentNumber);
}
