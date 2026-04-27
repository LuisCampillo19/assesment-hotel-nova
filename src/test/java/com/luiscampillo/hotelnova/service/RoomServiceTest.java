package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.exception.DuplicateRoomNumberException;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.model.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for RoomService.
 * Covers business rule R1: room_number must be unique on register.
 */
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock RoomDao roomDao;
    @InjectMocks RoomService service;

    private Room sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = new Room(0, "101", RoomType.SINGLE,
                new BigDecimal("120000"), RoomStatus.AVAILABLE);
    }

    @Test
    void createRoomSucceedsWhenNumberIsAvailable() {
        when(roomDao.findByRoomNumber("101")).thenReturn(Optional.empty());
        when(roomDao.save(any(Room.class))).thenAnswer(inv -> {
            Room r = inv.getArgument(0);
            r.setId(99);
            return r;
        });

        Room saved = service.createRoom(sampleRoom);

        assertEquals(99, saved.getId());
        verify(roomDao).save(sampleRoom);
    }

    @Test
    void createRoomThrowsWhenNumberAlreadyExists() {
        // R1: duplicated room_number must be rejected.
        Room existing = new Room(7, "101", RoomType.DOUBLE,
                new BigDecimal("180000"), RoomStatus.AVAILABLE);
        when(roomDao.findByRoomNumber("101")).thenReturn(Optional.of(existing));

        DuplicateRoomNumberException ex = assertThrows(
                DuplicateRoomNumberException.class,
                () -> service.createRoom(sampleRoom));

        assertTrue(ex.getMessage().contains("101"));
        verify(roomDao, never()).save(any());
    }
}
