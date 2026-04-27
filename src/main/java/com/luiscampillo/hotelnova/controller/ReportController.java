package com.luiscampillo.hotelnova.controller;

import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.report.CsvExporter;
import com.luiscampillo.hotelnova.service.ReservationService;
import com.luiscampillo.hotelnova.service.RoomService;
import com.luiscampillo.hotelnova.view.View;

import java.nio.file.Path;

public class ReportController {

    private final View view;
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CsvExporter exporter;

    public ReportController(View view,
                            RoomService roomService,
                            ReservationService reservationService,
                            CsvExporter exporter) {
        this.view               = view;
        this.roomService        = roomService;
        this.reservationService = reservationService;
        this.exporter           = exporter;
    }

    public void run() {
        String[] menu = {
                "Export room listing (CSV)",
                "Export active reservations (CSV)",
                "Back"
        };
        boolean running = true;
        while (running) {
            int choice = view.showMenu("Reports", menu);
            try {
                switch (choice) {
                    case 1 -> {
                        Path file = exporter.exportRooms(roomService.findAll());
                        view.showSuccess("Exported: " + file.toAbsolutePath());
                    }
                    case 2 -> {
                        Path file = exporter.exportActiveReservations(reservationService.findActive());
                        view.showSuccess("Exported: " + file.toAbsolutePath());
                    }
                    case 3, -1 -> running = false;
                    default -> view.showError("Invalid option");
                }
            } catch (BusinessException e) {
                view.showError(e.getMessage());
            } catch (RuntimeException e) {
                view.showError("Export failed: " + e.getMessage());
            }
        }
    }
}
