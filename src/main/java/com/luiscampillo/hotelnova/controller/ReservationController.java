package com.luiscampillo.hotelnova.controller;

import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.model.entity.Reservation;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.service.ReservationService;
import com.luiscampillo.hotelnova.view.View;

import java.time.LocalDate;

/**
 * Coordinates reservation operations. Holds a reference to the logged-in
 * User so every new reservation is stamped with its creator's id.
 */
public class ReservationController {

    private final View view;
    private final ReservationService reservationService;
    private final User currentUser;

    public ReservationController(View view,
                                 ReservationService reservationService,
                                 User currentUser) {
        this.view               = view;
        this.reservationService = reservationService;
        this.currentUser        = currentUser;
    }

    public void run() {
        String[] menu = {
                "List all reservations",
                "List active reservations",
                "List by guest id",
                "Show reservation detail",
                "Create reservation",
                "Check-in",
                "Check-out",
                "Cancel reservation",
                "Back"
        };
        boolean running = true;
        while (running) {
            int choice = view.showMenu("Reservations", menu);
            try {
                switch (choice) {
                    case 1  -> view.showReservations(reservationService.findAll());
                    case 2  -> view.showReservations(reservationService.findActive());
                    case 3  -> listByGuest();
                    case 4  -> showDetail();
                    case 5  -> createReservation();
                    case 6  -> checkIn();
                    case 7  -> checkOut();
                    case 8  -> cancelReservation();
                    case 9, -1 -> running = false;
                    default -> view.showError("Invalid option");
                }
            } catch (BusinessException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void listByGuest() {
        int guestId = view.askInt("Guest id");
        view.showReservations(reservationService.findByGuest(guestId));
    }

    private void showDetail() {
        int id = view.askInt("Reservation id");
        view.showReservation(reservationService.findById(id));
    }

    private void createReservation() {
        int guestId   = view.askInt("Guest id");
        int roomId    = view.askInt("Room id");
        LocalDate in  = view.askDate("Check-in date");
        LocalDate out = view.askDate("Check-out date");

        Reservation saved = reservationService.createReservation(
                guestId, roomId, currentUser.getId(), in, out);

        view.showSuccess("Reservation created. Id: " + saved.getId()
                + " | Total cost: $" + saved.getTotalCost());
    }

    private void checkIn() {
        int id = view.askInt("Reservation id for check-in");
        if (!view.askConfirm("Confirm check-in for reservation #" + id + "?")) return;
        reservationService.checkIn(id);
        view.showSuccess("Check-in completed");
    }

    private void checkOut() {
        int id = view.askInt("Reservation id for check-out");
        if (!view.askConfirm("Confirm check-out for reservation #" + id + "?")) return;
        reservationService.checkOut(id);
        view.showSuccess("Check-out completed");
    }

    private void cancelReservation() {
        int id = view.askInt("Reservation id to cancel");
        if (!view.askConfirm("Cancel reservation #" + id + "?")) return;
        if (reservationService.cancel(id)) view.showSuccess("Reservation cancelled");
        else view.showError("Could not cancel");
    }
}
