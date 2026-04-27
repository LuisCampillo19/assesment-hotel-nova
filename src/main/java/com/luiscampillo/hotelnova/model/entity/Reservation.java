package com.luiscampillo.hotelnova.model.entity;

import com.luiscampillo.hotelnova.model.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Booking that ties a Guest to a Room for a date range, created by a User.
 *
 * checkInDate / checkOutDate are the planned dates.
 * actualCheckIn / actualCheckOut are timestamps filled when the guest physically
 * arrives or leaves; both are null until those events occur.
 *
 * totalCost is computed once at creation:  nights * pricePerNight * (1 + IVA).
 */
public class Reservation {

    private int id;
    private int guestId;
    private int roomId;
    private int userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private ReservationStatus status;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;

    public Reservation() { }

    public Reservation(int id, int guestId, int roomId, int userId,
                       LocalDate checkInDate, LocalDate checkOutDate,
                       LocalDateTime actualCheckIn, LocalDateTime actualCheckOut,
                       ReservationStatus status, BigDecimal totalCost,
                       LocalDateTime createdAt) {
        this.id = id;
        this.guestId = guestId;
        this.roomId = roomId;
        this.userId = userId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.actualCheckIn = actualCheckIn;
        this.actualCheckOut = actualCheckOut;
        this.status = status;
        this.totalCost = totalCost;
        this.createdAt = createdAt;
    }

    /** Number of nights between check-in and check-out (exclusive of check-out). */
    public long nights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public LocalDateTime getActualCheckIn() { return actualCheckIn; }
    public void setActualCheckIn(LocalDateTime actualCheckIn) { this.actualCheckIn = actualCheckIn; }

    public LocalDateTime getActualCheckOut() { return actualCheckOut; }
    public void setActualCheckOut(LocalDateTime actualCheckOut) { this.actualCheckOut = actualCheckOut; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return id == r.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Reservation{id=" + id + ", guest=" + guestId + ", room=" + roomId
                + ", " + checkInDate + " -> " + checkOutDate
                + ", status=" + status + ", total=" + totalCost + "}";
    }
}
