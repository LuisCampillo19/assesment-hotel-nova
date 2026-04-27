package com.luiscampillo.hotelnova.view;

import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.entity.User;

/**
 * Shared formatting helpers so both view implementations render entities
 * the same way. Subclasses override the actual show* methods to render
 * these strings via console or via JOptionPane.
 */
public abstract class BaseView implements View {

    protected String formatUser(User u) {
        return String.format("[#%d] %s (%s) - %s - %s",
                u.getId(), u.getUsername(), u.getRole(),
                u.getFullName(), u.isActive() ? "Active" : "Inactive");
    }

    protected String formatRoom(Room r) {
        return String.format("[#%d] Room %s | %s | $%s/night | %s",
                r.getId(), r.getRoomNumber(), r.getType(),
                r.getPricePerNight(), r.getStatus());
    }

    protected String formatGuest(Guest g) {
        return String.format("[#%d] %s | %s %s | %s | %s",
                g.getId(), g.getDocumentNumber(),
                g.getFirstName(), g.getLastName(),
                g.getPhone() != null ? g.getPhone() : "-",
                g.isActive() ? "Active" : "Inactive");
    }

    protected String formatReservation(Reservation r) {
        return String.format("[#%d] Guest:%d Room:%d | %s -> %s | %s | $%s",
                r.getId(), r.getGuestId(), r.getRoomId(),
                r.getCheckInDate(), r.getCheckOutDate(),
                r.getStatus(), r.getTotalCost());
    }

    protected String formatReservationDetail(Reservation r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Reservation #").append(r.getId()).append('\n');
        sb.append("  Guest id:        ").append(r.getGuestId()).append('\n');
        sb.append("  Room id:         ").append(r.getRoomId()).append('\n');
        sb.append("  Created by user: ").append(r.getUserId()).append('\n');
        sb.append("  Check-in date:   ").append(r.getCheckInDate()).append('\n');
        sb.append("  Check-out date:  ").append(r.getCheckOutDate()).append('\n');
        sb.append("  Actual check-in: ").append(r.getActualCheckIn()).append('\n');
        sb.append("  Actual check-out:").append(r.getActualCheckOut()).append('\n');
        sb.append("  Status:          ").append(r.getStatus()).append('\n');
        sb.append("  Total cost:      $").append(r.getTotalCost());
        return sb.toString();
    }
}
