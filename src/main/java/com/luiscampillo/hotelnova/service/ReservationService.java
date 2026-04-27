package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.GuestDao;
import com.luiscampillo.hotelnova.dao.ReservationDao;
import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.db.ConnectionManager;
import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.exception.CheckoutWithoutCheckinException;
import com.luiscampillo.hotelnova.exception.EntityNotFoundException;
import com.luiscampillo.hotelnova.exception.InactiveGuestException;
import com.luiscampillo.hotelnova.exception.OverlappingReservationException;
import com.luiscampillo.hotelnova.exception.RoomNotAvailableException;
import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.ReservationStatus;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.util.AppLogger;
import com.luiscampillo.hotelnova.util.CostCalculator;
import com.luiscampillo.hotelnova.util.DateValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Reservation business operations.
 *
 * Enforces the reservation-related rules listed in the rubric:
 *   R2  room must be AVAILABLE
 *   R3  guest must be active
 *   R4  check-in strictly before check-out, no past dates
 *   R5  no overlapping reservation on the same room
 *   R6  check-out only allowed on ACTIVE reservations
 *   R7  total cost = nights * pricePerNight * (1 + IVA)
 *
 * Three operations run inside an explicit JDBC transaction
 * (setAutoCommit(false) + commit / rollback):
 *   createReservation   -> insert reservation
 *   checkIn             -> update reservation status + update room status
 *   checkOut            -> update reservation status + update room status
 *
 * On any RuntimeException inside the transactional block, the connection is
 * rolled back and the exception is re-thrown to the caller (controller),
 * which translates it to a user-friendly message in the view.
 */
public class ReservationService {

    private static final Logger LOG = AppLogger.getLogger(ReservationService.class);

    private final ConnectionManager cm;
    private final ReservationDao    reservationDao;
    private final RoomDao           roomDao;
    private final GuestDao          guestDao;
    private final BigDecimal        iva;

    public ReservationService(ConnectionManager cm,
                              ReservationDao reservationDao,
                              RoomDao roomDao,
                              GuestDao guestDao,
                              BigDecimal iva) {
        this.cm             = cm;
        this.reservationDao = reservationDao;
        this.roomDao        = roomDao;
        this.guestDao       = guestDao;
        this.iva            = iva;
    }

    // ========================================================================
    //  TRANSACTION 1 - createReservation
    // ========================================================================
    /**
     * Validates every business rule, computes cost, and inserts the
     * reservation inside a manual JDBC transaction.
     */
    public Reservation createReservation(int guestId, int roomId, int userId,
                                         LocalDate checkInDate,
                                         LocalDate checkOutDate) {
        // R4: validate dates first - cheap, fail fast before opening a connection.
        DateValidator.validateReservationDates(checkInDate, checkOutDate);

        // R3: guest must exist and be active.
        Guest guest = guestDao.findById(guestId)
                .orElseThrow(() -> new EntityNotFoundException("Guest", guestId));
        if (!guest.isActive()) {
            throw new InactiveGuestException(guest.getDocumentNumber());
        }

        // R2: room must exist and be AVAILABLE.
        Room room = roomDao.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomId));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException(room.getRoomNumber());
        }

        // R5: no other PENDING/ACTIVE reservation on this room overlapping the range.
        if (reservationDao.countOverlapping(roomId, checkInDate, checkOutDate) > 0) {
            throw new OverlappingReservationException(room.getRoomNumber());
        }

        // R7: compute cost.
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal cost = CostCalculator.compute(room.getPricePerNight(), nights, iva);

        // Build the entity to persist.
        Reservation reservation = new Reservation();
        reservation.setGuestId(guestId);
        reservation.setRoomId(roomId);
        reservation.setUserId(userId);
        reservation.setCheckInDate(checkInDate);
        reservation.setCheckOutDate(checkOutDate);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setTotalCost(cost);

        // -- Transactional write ------------------------------------------------
        try (Connection conn = cm.getConnection(false)) {
            try {
                Reservation saved = reservationDao.save(conn, reservation);
                conn.commit();
                LOG.info("Reservation created (committed): " + saved);
                return saved;
            } catch (RuntimeException ex) {
                safeRollback(conn, "createReservation");
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("JDBC failure during createReservation", e);
        }
    }

    // ========================================================================
    //  TRANSACTION 2 - checkIn
    // ========================================================================
    /**
     * Marks a PENDING reservation as ACTIVE and the room as OCCUPIED.
     * Both updates run in the same transaction.
     */
    public void checkIn(int reservationId) {
        Reservation r = findById(reservationId);
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(
                    "Cannot check in reservation #" + reservationId
                            + " - current status is " + r.getStatus());
        }

        r.setStatus(ReservationStatus.ACTIVE);
        r.setActualCheckIn(LocalDateTime.now());

        try (Connection conn = cm.getConnection(false)) {
            try {
                reservationDao.update(conn, r);
                roomDao.updateStatus(conn, r.getRoomId(), RoomStatus.OCCUPIED);
                conn.commit();
                LOG.info("Check-in completed for reservation #" + reservationId);
            } catch (RuntimeException ex) {
                safeRollback(conn, "checkIn");
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("JDBC failure during checkIn", e);
        }
    }

    // ========================================================================
    //  TRANSACTION 3 - checkOut
    // ========================================================================
    /**
     * R6: only ACTIVE reservations can be checked out.
     * Marks the reservation COMPLETED and frees the room (AVAILABLE).
     */
    public void checkOut(int reservationId) {
        Reservation r = findById(reservationId);
        if (r.getStatus() != ReservationStatus.ACTIVE) {
            throw new CheckoutWithoutCheckinException(reservationId);
        }

        r.setStatus(ReservationStatus.COMPLETED);
        r.setActualCheckOut(LocalDateTime.now());

        try (Connection conn = cm.getConnection(false)) {
            try {
                reservationDao.update(conn, r);
                roomDao.updateStatus(conn, r.getRoomId(), RoomStatus.AVAILABLE);
                conn.commit();
                LOG.info("Check-out completed for reservation #" + reservationId);
            } catch (RuntimeException ex) {
                safeRollback(conn, "checkOut");
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("JDBC failure during checkOut", e);
        }
    }

    // ========================================================================
    //  Non-transactional reads / single-statement updates
    // ========================================================================

    public Reservation findById(int id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation", id));
    }

    public List<Reservation> findAll()                   { return reservationDao.findAll(); }
    public List<Reservation> findActive()                { return reservationDao.findByStatus(ReservationStatus.ACTIVE); }
    public List<Reservation> findByGuest(int guestId)    { return reservationDao.findByGuestId(guestId); }
    public List<Reservation> findByRoom(int roomId)      { return reservationDao.findByRoomId(roomId); }

    /** Cancels a PENDING reservation. Single update - no explicit transaction needed. */
    public boolean cancel(int reservationId) {
        Reservation r = findById(reservationId);
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING reservations can be cancelled. Current: " + r.getStatus());
        }
        r.setStatus(ReservationStatus.CANCELLED);
        boolean ok = reservationDao.update(r);
        if (ok) LOG.info("Reservation cancelled: #" + reservationId);
        return ok;
    }

    // ========================================================================
    //  Helpers
    // ========================================================================

    private void safeRollback(Connection conn, String operation) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                LOG.warning("Rolled back transaction in " + operation);
            }
        } catch (SQLException e) {
            LOG.severe("Rollback failed in " + operation + ": " + e.getMessage());
        }
    }
}
