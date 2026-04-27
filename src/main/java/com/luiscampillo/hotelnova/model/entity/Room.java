package com.luiscampillo.hotelnova.model.entity;

import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.model.enums.RoomType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Physical hotel room. Identified externally by roomNumber (unique).
 * The "status" field is updated transactionally during check-in / check-out.
 */
public class Room {

    private int id;
    private String roomNumber;
    private RoomType type;
    private BigDecimal pricePerNight;
    private RoomStatus status;

    public Room() { }

    public Room(int id, String roomNumber, RoomType type,
                BigDecimal pricePerNight, RoomStatus status) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room r)) return false;
        return id == r.id && Objects.equals(roomNumber, r.roomNumber);
    }

    @Override
    public int hashCode() { return Objects.hash(id, roomNumber); }

    @Override
    public String toString() {
        return "Room{id=" + id + ", number='" + roomNumber + "', type=" + type
                + ", price=" + pricePerNight + ", status=" + status + "}";
    }
}
