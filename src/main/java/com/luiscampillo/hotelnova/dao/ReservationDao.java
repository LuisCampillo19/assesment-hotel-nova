package com.luiscampillo.hotelnova.dao;

import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.enums.ReservationStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface ReservationDao extends GenericDao<Reservation, Integer> {

    /** All reservations belonging to a single guest. */
    List<Reservation> findByGuestId(int guestId);

    /** All reservations on a single room (any status). */
    List<Reservation> findByRoomId(int roomId);

    /** Reservations in the requested status (e.g. all ACTIVE for the report). */
    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Counts existing PENDING / ACTIVE reservations on the given room whose
     * date range overlaps [checkIn, checkOut). Used by ReservationService
     * to enforce the "no overlapping" business rule.
     */
    int countOverlapping(int roomId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Saves a reservation using a caller-supplied Connection.
     * Required because reservation creation participates in a JDBC transaction
     * that also updates the room status.
     */
    Reservation save(Connection conn, Reservation reservation);

    /** Updates a reservation using a caller-supplied Connection (transactional). */
    boolean update(Connection conn, Reservation reservation);
}
