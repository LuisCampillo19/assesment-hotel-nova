package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.GuestDao;
import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.exception.EntityNotFoundException;
import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.util.AppLogger;

import java.util.List;
import java.util.logging.Logger;

/**
 * Business operations on guests. Enforces document-number uniqueness at the
 * service layer (the database also enforces it via a UNIQUE constraint, but
 * checking first lets us return a friendly error instead of a JDBC exception).
 */
public class GuestService {

    private static final Logger LOG = AppLogger.getLogger(GuestService.class);

    private final GuestDao guestDao;

    public GuestService(GuestDao guestDao) {
        this.guestDao = guestDao;
    }

    public Guest createGuest(Guest guest) {
        if (guestDao.findByDocumentNumber(guest.getDocumentNumber()).isPresent()) {
            throw new BusinessException(
                    "Guest already registered with document: " + guest.getDocumentNumber());
        }
        guest.setActive(true);
        Guest saved = guestDao.save(guest);
        LOG.info("Guest created: " + saved);
        return saved;
    }

    public Guest findById(int id) {
        return guestDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guest", id));
    }

    public Guest findByDocument(String documentNumber) {
        return guestDao.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Guest", documentNumber));
    }

    public List<Guest> findAll() { return guestDao.findAll(); }

    public boolean update(Guest guest) {
        boolean ok = guestDao.update(guest);
        if (ok) LOG.info("Guest updated: " + guest);
        return ok;
    }

    /** Soft-disable a guest. Preserves their reservation history. */
    public boolean deactivate(int id) {
        Guest g = findById(id);
        g.setActive(false);
        boolean ok = guestDao.update(g);
        if (ok) LOG.info("Guest deactivated: " + g);
        return ok;
    }

    public boolean activate(int id) {
        Guest g = findById(id);
        g.setActive(true);
        boolean ok = guestDao.update(g);
        if (ok) LOG.info("Guest activated: " + g);
        return ok;
    }
}
