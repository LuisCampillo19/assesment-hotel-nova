package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.GuestDao;
import com.luiscampillo.hotelnova.dao.ReservationDao;
import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.db.ConnectionManager;
import com.luiscampillo.hotelnova.exception.CheckoutWithoutCheckinException;
import com.luiscampillo.hotelnova.exception.InactiveGuestException;
import com.luiscampillo.hotelnova.exception.OverlappingReservationException;
import com.luiscampillo.hotelnova.exception.RoomNotAvailableException;
import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.ReservationStatus;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.model.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ReservationService.
 * Covers business rules:
 *   R2 - room must be AVAILABLE
 *   R3 - guest must be active
 *   R5 - no overlapping reservations on the same room
 *   R6 - check-out only allowed on ACTIVE reservations
 *
 * Connection-related calls are stubbed; no real database is touched.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ConnectionManager   cm;
    @Mock ReservationDao      reservationDao;
    @Mock RoomDao             roomDao;
    @Mock GuestDao            guestDao;
    @Mock Connection          connection;

    ReservationService service;

    private Guest activeGuest;
    private Guest inactiveGuest;
    private Room  availableRoom;
    private Room  occupiedRoom;
    private final LocalDate FUTURE_IN  = LocalDate.now().plusDays(1);
    private final LocalDate FUTURE_OUT = LocalDate.now().plusDays(4);

    @BeforeEach
    void setUp() {
        service = new ReservationService(
                cm, reservationDao, roomDao, guestDao, new BigDecimal("0.19"));

        activeGuest   = new Guest(1, "1001", "Maria",  "Rodriguez", "300", "m@x.com", true,  null);
        inactiveGuest = new Guest(2, "1002", "Carlos", "Gomez",     "300", "c@x.com", false, null);

        availableRoom = new Room(10, "101", RoomType.SINGLE,
                new BigDecimal("100000"), RoomStatus.AVAILABLE);
        occupiedRoom  = new Room(11, "102", RoomType.SINGLE,
                new BigDecimal("100000"), RoomStatus.OCCUPIED);
    }

    // ---------- R3: inactive guest is rejected ----------------------------

    @Test
    void createReservationFailsWhenGuestIsInactive() {
        when(guestDao.findById(2)).thenReturn(Optional.of(inactiveGuest));

        InactiveGuestException ex = assertThrows(
                InactiveGuestException.class,
                () -> service.createReservation(2, 10, 99, FUTURE_IN, FUTURE_OUT));

        assertTrue(ex.getMessage().contains("1002"));
        verify(reservationDao, never()).save(any(Connection.class), any());
    }

    // ---------- R2: room must be AVAILABLE --------------------------------

    @Test
    void createReservationFailsWhenRoomIsNotAvailable() {
        when(guestDao.findById(1)).thenReturn(Optional.of(activeGuest));
        when(roomDao.findById(11)).thenReturn(Optional.of(occupiedRoom));

        RoomNotAvailableException ex = assertThrows(
                RoomNotAvailableException.class,
                () -> service.createReservation(1, 11, 99, FUTURE_IN, FUTURE_OUT));

        assertTrue(ex.getMessage().contains("102"));
        verify(reservationDao, never()).save(any(Connection.class), any());
    }

    // ---------- R5: no overlap with existing reservation ------------------

    @Test
    void createReservationFailsWhenOverlapping() throws Exception {
        when(guestDao.findById(1)).thenReturn(Optional.of(activeGuest));
        when(roomDao.findById(10)).thenReturn(Optional.of(availableRoom));
        when(reservationDao.countOverlapping(10, FUTURE_IN, FUTURE_OUT)).thenReturn(1);

        OverlappingReservationException ex = assertThrows(
                OverlappingReservationException.class,
                () -> service.createReservation(1, 10, 99, FUTURE_IN, FUTURE_OUT));

        assertTrue(ex.getMessage().contains("101"));
        verify(reservationDao, never()).save(any(Connection.class), any());
    }

    // ---------- Happy path: every rule passes -----------------------------

    @Test
    void createReservationSucceedsWhenEveryRulePasses() throws Exception {
        when(guestDao.findById(1)).thenReturn(Optional.of(activeGuest));
        when(roomDao.findById(10)).thenReturn(Optional.of(availableRoom));
        when(reservationDao.countOverlapping(10, FUTURE_IN, FUTURE_OUT)).thenReturn(0);
        when(cm.getConnection(false)).thenReturn(connection);
        when(reservationDao.save(eq(connection), any(Reservation.class)))
                .thenAnswer(inv -> {
                    Reservation r = inv.getArgument(1);
                    r.setId(500);
                    return r;
                });

        Reservation saved = service.createReservation(1, 10, 99, FUTURE_IN, FUTURE_OUT);

        assertEquals(500, saved.getId());
        assertEquals(ReservationStatus.PENDING, saved.getStatus());
        // 3 nights * 100000 * 1.19 = 357000.00
        assertEquals(0, saved.getTotalCost().compareTo(new BigDecimal("357000.00")));
        verify(connection).commit();
        verify(connection, never()).rollback();
    }

    // ---------- R6: check-out without check-in is rejected ----------------

    @Test
    void checkOutFailsWhenReservationIsNotActive() {
        Reservation pending = new Reservation();
        pending.setId(7);
        pending.setRoomId(10);
        pending.setStatus(ReservationStatus.PENDING);
        when(reservationDao.findById(7)).thenReturn(Optional.of(pending));

        assertThrows(CheckoutWithoutCheckinException.class, () -> service.checkOut(7));
        verify(roomDao, never()).updateStatus(any(Connection.class), anyInt(), any());
    }

    // ---------- check-out happy path with rollback verification -----------

    @Test
    void checkOutCommitsBothUpdatesOnSuccess() throws Exception {
        Reservation active = new Reservation();
        active.setId(7);
        active.setRoomId(10);
        active.setStatus(ReservationStatus.ACTIVE);
        active.setActualCheckIn(LocalDateTime.now().minusDays(1));

        when(reservationDao.findById(7)).thenReturn(Optional.of(active));
        when(cm.getConnection(false)).thenReturn(connection);
        when(reservationDao.update(eq(connection), any(Reservation.class))).thenReturn(true);
        when(roomDao.updateStatus(eq(connection), eq(10), eq(RoomStatus.AVAILABLE))).thenReturn(true);

        service.checkOut(7);

        assertEquals(ReservationStatus.COMPLETED, active.getStatus());
        assertNotNull(active.getActualCheckOut());
        verify(connection).commit();
        verify(connection, never()).rollback();
    }
}
