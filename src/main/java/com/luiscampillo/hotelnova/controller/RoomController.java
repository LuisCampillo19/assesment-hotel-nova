package com.luiscampillo.hotelnova.controller;

import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.model.entity.Room;
import com.luiscampillo.hotelnova.model.enums.RoomStatus;
import com.luiscampillo.hotelnova.model.enums.RoomType;
import com.luiscampillo.hotelnova.service.RoomService;
import com.luiscampillo.hotelnova.view.View;

import java.math.BigDecimal;

public class RoomController {

    private final View view;
    private final RoomService roomService;

    public RoomController(View view, RoomService roomService) {
        this.view        = view;
        this.roomService = roomService;
    }

    public void run() {
        String[] menu = {
                "List all rooms",
                "List available rooms",
                "Find room by number",
                "Register new room",
                "Update room",
                "Delete room",
                "Back"
        };
        boolean running = true;
        while (running) {
            int choice = view.showMenu("Rooms", menu);
            try {
                switch (choice) {
                    case 1  -> view.showRooms(roomService.findAll());
                    case 2  -> view.showRooms(roomService.findAvailable());
                    case 3  -> findByNumber();
                    case 4  -> createRoom();
                    case 5  -> updateRoom();
                    case 6  -> deleteRoom();
                    case 7, -1 -> running = false;
                    default -> view.showError("Invalid option");
                }
            } catch (BusinessException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void findByNumber() {
        String number = view.askString("Room number");
        view.showRoom(roomService.findByNumber(number));
    }

    private void createRoom() {
        String number = view.askString("Room number");
        RoomType type = pickRoomType();
        BigDecimal price = view.askDecimal("Price per night");

        Room room = new Room();
        room.setRoomNumber(number);
        room.setType(type);
        room.setPricePerNight(price);
        room.setStatus(RoomStatus.AVAILABLE);

        Room saved = roomService.createRoom(room);
        view.showSuccess("Room created with id " + saved.getId());
    }

    private void updateRoom() {
        int id = view.askInt("Room id to update");
        Room existing = roomService.findById(id);
        view.showRoom(existing);

        existing.setPricePerNight(view.askDecimal("New price per night"));
        existing.setStatus(pickRoomStatus());

        if (roomService.update(existing)) {
            view.showSuccess("Room updated");
        } else {
            view.showError("Update failed");
        }
    }

    private void deleteRoom() {
        int id = view.askInt("Room id to delete");
        if (!view.askConfirm("Delete room #" + id + "?")) return;
        if (roomService.deleteById(id)) {
            view.showSuccess("Room deleted");
        } else {
            view.showError("Delete failed");
        }
    }

    private RoomType pickRoomType() {
        String[] options = { "SINGLE", "DOUBLE", "SUITE" };
        int choice = view.showMenu("Room type", options);
        return switch (choice) {
            case 2 -> RoomType.DOUBLE;
            case 3 -> RoomType.SUITE;
            default -> RoomType.SINGLE;
        };
    }

    private RoomStatus pickRoomStatus() {
        String[] options = { "AVAILABLE", "OCCUPIED", "MAINTENANCE" };
        int choice = view.showMenu("Room status", options);
        return switch (choice) {
            case 2 -> RoomStatus.OCCUPIED;
            case 3 -> RoomStatus.MAINTENANCE;
            default -> RoomStatus.AVAILABLE;
        };
    }
}
