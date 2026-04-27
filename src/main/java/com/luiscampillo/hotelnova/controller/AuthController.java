package com.luiscampillo.hotelnova.controller;

import com.luiscampillo.hotelnova.exception.BusinessException;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.service.AuthService;
import com.luiscampillo.hotelnova.view.View;

/**
 * Walks the user through the login flow. Returns the authenticated User
 * on success, null after three failed attempts.
 */
public class AuthController {

    private static final int MAX_ATTEMPTS = 3;

    private final View view;
    private final AuthService authService;

    public AuthController(View view, AuthService authService) {
        this.view        = view;
        this.authService = authService;
    }

    public User login() {
        view.showMessage("=== HotelNova - Login ===");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String username = view.askString("Username");
                if (username.isEmpty()) return null;
                String password = view.askPassword("Password");

                User user = authService.login(username, password);
                view.showSuccess("Welcome, " + user.getFullName());
                return user;
            } catch (BusinessException e) {
                int remaining = MAX_ATTEMPTS - attempt;
                view.showError(e.getMessage()
                        + (remaining > 0 ? " (" + remaining + " attempt(s) left)" : ""));
            }
        }
        return null;
    }
}
