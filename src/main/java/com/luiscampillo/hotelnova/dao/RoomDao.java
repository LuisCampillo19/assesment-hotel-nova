package com.luiscampillo.hotelnova.dao;

import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface RoomDao extends GenericDao<Room, Integer> {

    /** Looks up a room by its public number ("101", "203", ...). */
    Optional<Room> findByRoomNumber(String roomNumber);

    /** Returns every room currently in AVAILABLE status. */
    List<Room> findAvailable();

    /** Updates only the status column. Auto-commit version. */
    boolean updateStatus(int roomId, RoomStatus status);

    /**
     * Same as above but uses a caller-supplied Connection so the update can
     * participate in an outer transaction (check-in / check-out flows).
     */
    boolean updateStatus(Connection conn, int roomId, RoomStatus status);
}
