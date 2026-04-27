package com.luiscampillo.hotelnova.controller;

import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.model.entity.Guest;
import com.luiscampillo.hotelnova.service.GuestService;
import com.luiscampillo.hotelnova.view.View;

public class GuestController {

    private final View view;
    private final GuestService guestService;

    public GuestController(View view, GuestService guestService) {
        this.view         = view;
        this.guestService = guestService;
    }

    public void run() {
        String[] menu = {
                "List all guests",
                "Find guest by document",
                "Register new guest",
                "Update guest",
                "Deactivate guest",
                "Activate guest",
                "Back"
        };
        boolean running = true;
        while (running) {
            int choice = view.showMenu("Guests", menu);
            try {
                switch (choice) {
                    case 1  -> view.showGuests(guestService.findAll());
                    case 2  -> findByDocument();
                    case 3  -> createGuest();
                    case 4  -> updateGuest();
                    case 5  -> deactivateGuest();
                    case 6  -> activateGuest();
                    case 7, -1 -> running = false;
                    default -> view.showError("Invalid option");
                }
            } catch (BusinessException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void findByDocument() {
        String doc = view.askString("Document number");
        view.showGuest(guestService.findByDocument(doc));
    }

    private void createGuest() {
        Guest g = new Guest();
        g.setDocumentNumber(view.askString("Document number"));
        g.setFirstName(view.askString("First name"));
        g.setLastName(view.askString("Last name"));
        g.setPhone(view.askString("Phone"));
        g.setEmail(view.askString("Email"));

        Guest saved = guestService.createGuest(g);
        view.showSuccess("Guest created with id " + saved.getId());
    }

    private void updateGuest() {
        int id = view.askInt("Guest id");
        Guest existing = guestService.findById(id);
        view.showGuest(existing);

        existing.setFirstName(view.askString("New first name"));
        existing.setLastName(view.askString("New last name"));
        existing.setPhone(view.askString("New phone"));
        existing.setEmail(view.askString("New email"));

        if (guestService.update(existing)) view.showSuccess("Guest updated");
        else view.showError("Update failed");
    }

    private void deactivateGuest() {
        int id = view.askInt("Guest id to deactivate");
        if (guestService.deactivate(id)) view.showSuccess("Guest deactivated");
        else view.showError("Could not deactivate");
    }

    private void activateGuest() {
        int id = view.askInt("Guest id to activate");
        if (guestService.activate(id)) view.showSuccess("Guest activated");
        else view.showError("Could not activate");
    }
}
