package com.luiscampillo.hotelnova.view;

import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Text-mode UI. Reads from System.in, writes to System.out.
 *
 * Password input uses System.console().readPassword() when available
 * (real terminals) and falls back to a normal read in IDE consoles where
 * System.console() is null.
 */
public class ConsoleView extends BaseView {

    private final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ---- Output --------------------------------------------------

    @Override
    public void showMessage(String msg) {
        System.out.println(msg);
    }

    @Override
    public void showError(String msg) {
        System.out.println("[ERROR] " + msg);
    }

    @Override
    public void showSuccess(String msg) {
        System.out.println("[OK] " + msg);
    }

    // ---- Input ---------------------------------------------------

    @Override
    public String askString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    @Override
    public String askPassword(String prompt) {
        if (System.console() != null) {
            char[] pwd = System.console().readPassword(prompt + ": ");
            return new String(pwd);
        }
        // Fallback for IDE consoles where System.console() is null.
        return askString(prompt);
    }

    @Override
    public int askInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(askString(prompt));
            } catch (NumberFormatException e) {
                showError("Please enter a valid integer.");
            }
        }
    }

    @Override
    public BigDecimal askDecimal(String prompt) {
        while (true) {
            try {
                return new BigDecimal(askString(prompt));
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
        String input = askString(prompt + " (y/n)").toLowerCase();
        return input.equals("y") || input.equals("yes") || input.equals("s") || input.equals("si");
    }

    // ---- Menu ----------------------------------------------------

    @Override
    public int showMenu(String title, String[] options) {
        System.out.println();
        System.out.println("==== " + title + " ====");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, options[i]);
        }
        System.out.println("  0. Cancel / back");
        int choice = askInt("Choose option");
        if (choice == 0) return -1;
        if (choice < 1 || choice > options.length) {
            showError("Invalid option");
            return showMenu(title, options);
        }
        return choice;
    }

    // ---- Listings ------------------------------------------------

    @Override
    public void showUser(User user) {
        showMessage(formatUser(user));
    }

    @Override
    public void showRoom(Room room) {
        showMessage(formatRoom(room));
    }

    @Override
    public void showRooms(List<Room> rooms) {
        if (rooms.isEmpty()) { showMessage("(no rooms)"); return; }
        showMessage("--- Rooms (" + rooms.size() + ") ---");
        rooms.forEach(r -> showMessage(formatRoom(r)));
    }

    @Override
    public void showGuest(Guest guest) {
        showMessage(formatGuest(guest));
    }

    @Override
    public void showGuests(List<Guest> guests) {
        if (guests.isEmpty()) { showMessage("(no guests)"); return; }
        showMessage("--- Guests (" + guests.size() + ") ---");
        guests.forEach(g -> showMessage(formatGuest(g)));
    }

    @Override
    public void showReservation(Reservation reservation) {
        showMessage(formatReservationDetail(reservation));
    }

    @Override
    public void showReservations(List<Reservation> reservations) {
        if (reservations.isEmpty()) { showMessage("(no reservations)"); return; }
        showMessage("--- Reservations (" + reservations.size() + ") ---");
        reservations.forEach(r -> showMessage(formatReservation(r)));
    }
}
