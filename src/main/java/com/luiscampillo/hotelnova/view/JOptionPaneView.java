package com.luiscampillo.hotelnova.view;

import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.entity.User;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI based on JOptionPane dialogs - the view explicitly requested by the
 * rubric. All interaction happens through modal dialogs; there is no main
 * window or persistent form.
 *
 * For lists we render a single multi-line message so the user sees the
 * whole table at once. For per-row navigation use the controllers' menus.
 */
public class JOptionPaneView extends BaseView {

    private static final String   APP_TITLE = "HotelNova";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---- Output --------------------------------------------------

    @Override
    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, APP_TITLE, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(null, msg, APP_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    // ---- Input ---------------------------------------------------

    @Override
    public String askString(String prompt) {
        String value = JOptionPane.showInputDialog(null, prompt, APP_TITLE, JOptionPane.QUESTION_MESSAGE);
        if (value == null) return "";   // user cancelled
        return value.trim();
    }

    @Override
    public String askPassword(String prompt) {
        JPasswordField field = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(
                null, field, prompt, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return "";
        return new String(field.getPassword());
    }

    @Override
    public int askInt(String prompt) {
        while (true) {
            String input = askString(prompt);
            if (input.isEmpty()) return 0;
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showError("Please enter a valid integer.");
            }
        }
    }

    @Override
    public BigDecimal askDecimal(String prompt) {
        while (true) {
            String input = askString(prompt);
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                showError("Please enter a valid decimal number (e.g. 120000.50).");
            }
        }
    }

    @Override
    public LocalDate askDate(String prompt) {
        while (true) {
            String input = askString(prompt + " (yyyy-MM-dd)");
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                showError("Please enter a valid date in yyyy-MM-dd format.");
            }
        }
    }

    @Override
    public boolean askConfirm(String prompt) {
        int answer = JOptionPane.showConfirmDialog(
                null, prompt, APP_TITLE, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    // ---- Menu ----------------------------------------------------

    @Override
    public int showMenu(String title, String[] options) {
        int idx = JOptionPane.showOptionDialog(
                null, "Choose an option:", title,
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if (idx == JOptionPane.CLOSED_OPTION) return -1;
        return idx + 1;   // dialog returns 0-based; we use 1-based externally.
    }

    // ---- Listings ------------------------------------------------

    @Override
    public void showUser(User user) { showMessage(formatUser(user)); }

    @Override
    public void showRoom(Room room) { showMessage(formatRoom(room)); }

    @Override
    public void showRooms(List<Room> rooms) {
        if (rooms.isEmpty()) { showMessage("(no rooms)"); return; }
        String body = "Rooms (" + rooms.size() + "):\n\n"
                + rooms.stream().map(this::formatRoom).collect(Collectors.joining("\n"));
        showMessage(body);
    }

    @Override
    public void showGuest(Guest guest) { showMessage(formatGuest(guest)); }

    @Override
    public void showGuests(List<Guest> guests) {
        if (guests.isEmpty()) { showMessage("(no guests)"); return; }
        String body = "Guests (" + guests.size() + "):\n\n"
                + guests.stream().map(this::formatGuest).collect(Collectors.joining("\n"));
        showMessage(body);
    }

    @Override
    public void showReservation(Reservation reservation) {
        showMessage(formatReservationDetail(reservation));
    }

    @Override
    public void showReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) { showMessage("(no reservations)"); return; }
        String body = "Reservations (" + reservations.size() + "):\n\n"
                + reservations.stream().map(this::formatReservation).collect(Collectors.joining("\n"));
        showMessage(body);
    }
}
