package com.luiscampillo.hotelnova.view;

import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contract for every UI implementation. Controllers depend on this
 * interface only - they never know whether they are running over the
 * console or over JOptionPane.
 *
 * Convention for showMenu():
 *   - returns 1..N when the user picks an option (1-based)
 *   - returns -1 when the user cancels / closes the dialog
 */
public interface View {

    // ---- Output --------------------------------------------------
    void showMessage(String msg);
    void showError(String msg);
    void showSuccess(String msg);

    // ---- Input ---------------------------------------------------
    String     askString(String prompt);
    String     askPassword(String prompt);
    int        askInt(String prompt);
    BigDecimal askDecimal(String prompt);
    LocalDate  askDate(String prompt);
    boolean    askConfirm(String prompt);

    // ---- Menu ----------------------------------------------------
    int showMenu(String title, String[] options);

    // ---- Domain listings -----------------------------------------
    void showUser(User user);
    void showRoom(Room room);
    void showRooms(List<Room> rooms);
    void showGuest(Guest guest);
    void showGuests(List<Guest> guests);
    void showReservation(Reservation reservation);
    void showReservations(List<Reservation> reservations);
}
