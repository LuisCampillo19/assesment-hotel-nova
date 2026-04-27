package com.luiscampillo.hotelnova;

import com.luiscampillo.hotelnova.config.AppConfig;
import com.luiscampillo.hotelnova.controller.AuthController;
import com.luiscampillo.hotelnova.controller.GuestController;
import com.luiscampillo.hotelnova.controller.ReportController;
import com.luiscampillo.hotelnova.controller.ReservationController;
import com.luiscampillo.hotelnova.controller.RoomController;
import com.luiscampillo.hotelnova.dao.GuestDao;
import com.luiscampillo.hotelnova.dao.ReservationDao;
import com.luiscampillo.hotelnova.dao.RoomDao;
import com.luiscampillo.hotelnova.dao.UserDao;
import com.luiscampillo.hotelnova.dao.impl.GuestDaoImpl;
import com.luiscampillo.hotelnova.dao.impl.ReservationDaoImpl;
import com.luiscampillo.hotelnova.dao.impl.RoomDaoImpl;
import com.luiscampillo.hotelnova.dao.impl.UserDaoImpl;
import com.luiscampillo.hotelnova.db.ConnectionManager;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.report.CsvExporter;
import com.luiscampillo.hotelnova.service.AuthService;
import com.luiscampillo.hotelnova.service.GuestService;
import com.luiscampillo.hotelnova.service.ReservationService;
import com.luiscampillo.hotelnova.service.RoomService;
import com.luiscampillo.hotelnova.util.AppLogger;
import com.luiscampillo.hotelnova.view.ConsoleView;
import com.luiscampillo.hotelnova.view.JOptionPaneView;
import com.luiscampillo.hotelnova.view.View;

import java.util.logging.Logger;

/**
 * Composition root. Wires every layer together and runs the main loop.
 *
 *   1. Load configuration (app.properties + database.properties).
 *   2. Build the connection manager (singleton, JDBC driver loaded here).
 *   3. Build the DAOs (no shared state - safe to instantiate at startup).
 *   4. Build the services, injecting the DAOs they need.
 *   5. Build the view picked by view.type in app.properties.
 *   6. Run the login flow.
 *   7. Loop the main menu, dispatching to the per-entity controllers.
 */
public class Main {

    private static final Logger LOG = AppLogger.getLogger(Main.class);

    public static void main(String[] args) {
        // 1. Configuration
        AppConfig config = AppConfig.getInstance();
        LOG.info(config.getAppName() + " v" + config.getAppVersion() + " starting up");

        // 2. Connection
        ConnectionManager cm = ConnectionManager.getInstance();

        // 3. DAOs
        UserDao        userDao        = new UserDaoImpl();
        GuestDao       guestDao       = new GuestDaoImpl();
        RoomDao        roomDao        = new RoomDaoImpl();
        ReservationDao reservationDao = new ReservationDaoImpl();

        // 4. Services
        AuthService        authService        = new AuthService(userDao);
        RoomService        roomService        = new RoomService(roomDao);
        GuestService       guestService       = new GuestService(guestDao);
        ReservationService reservationService = new ReservationService(
                cm, reservationDao, roomDao, guestDao, config.getIva());

        // 5. View
        View view = createView(config.getViewType());

        // 6. Login
        AuthController authController = new AuthController(view, authService);
        User currentUser = authController.login();
        if (currentUser == null) {
            view.showError("Authentication failed. Goodbye.");
            LOG.warning("Application shutting down: authentication failed");
            return;
        }

        // 7. Post-login controllers
        RoomController        roomCtrl   = new RoomController(view, roomService);
        GuestController       guestCtrl  = new GuestController(view, guestService);
        ReservationController resCtrl    = new ReservationController(view, reservationService, currentUser);
        ReportController      reportCtrl = new ReportController(view, roomService, reservationService, new CsvExporter());

        // Main loop
        String[] mainMenu = { "Rooms", "Guests", "Reservations", "Reports", "Logout" };
        boolean running = true;
        while (running) {
            int choice = view.showMenu("HotelNova - " + currentUser.getUsername(), mainMenu);
            switch (choice) {
                case 1  -> roomCtrl.run();
                case 2  -> guestCtrl.run();
                case 3  -> resCtrl.run();
                case 4  -> reportCtrl.run();
                case 5, -1 -> running = false;
                default -> view.showError("Invalid option");
            }
        }

        view.showMessage("Goodbye, " + currentUser.getFullName());
        LOG.info("Application shutting down: user logout");
    }

    /** Factory: returns the view implementation requested in app.properties. */
    private static View createView(String type) {
        return "console".equalsIgnoreCase(type) ? new ConsoleView() : new JOptionPaneView();
    }
}
