package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.exception.DuplicateRoomNumberException;
import com.luiscampillo.hotelnova.exception.EntityNotFoundException;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.util.AppLogger;

import java.util.List;
import java.util.logging.Logger;

/**
 * Business operations on rooms. Enforces business rule R1: room_number unique.
 */
public class RoomService {

    private static final Logger LOG = AppLogger.getLogger(RoomService.class);

    private final RoomDao roomDao;

    public RoomService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    /**
     * Creates a new room. Throws DuplicateRoomNumberException if the
     * room_number is already taken (rule R1).
     */
    public Room createRoom(Room room) {
        if (roomDao.findByRoomNumber(room.getRoomNumber()).isPresent()) {
            throw new DuplicateRoomNumberException(room.getRoomNumber());
        }
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
        Room saved = roomDao.save(room);
        LOG.info("Room created: " + saved);
        return saved;
    }

    public Room findById(int id) {
        return roomDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room", id));
    }

    public Room findByNumber(String roomNumber) {
        return roomDao.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new EntityNotFoundException("Room", roomNumber));
    }

    public List<Room> findAll()       { return roomDao.findAll(); }
    public List<Room> findAvailable() { return roomDao.findAvailable(); }

    public boolean update(Room room) {
        boolean ok = roomDao.update(room);
        if (ok) LOG.info("Room updated: " + room);
        return ok;
    }

    public boolean deleteById(int id) {
        boolean ok = roomDao.deleteById(id);
        if (ok) LOG.info("Room deleted: id=" + id);
        return ok;
    }
}
