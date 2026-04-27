package com.luiscampillo.hotelnova.service;

import com.luiscampillo.hotelnova.dao.UserDao;
import com.luiscampillo.hotelnova.exception.AuthenticationException;
import com.luiscampillo.hotelnova.model.entity.User;
import com.luiscampillo.hotelnova.util.AppLogger;
import com.luiscampillo.hotelnova.util.PasswordEncoder;

import java.util.logging.Logger;

/**
 * Authenticates system users by username + plaintext password.
 *
 * Always returns the same generic error message for unknown user / wrong
 * password / inactive user when triggered from the login flow, so an
 * attacker cannot probe whether a username exists. Logging captures the
 * actual reason for the operator.
 */
public class AuthService {

    private static final Logger LOG = AppLogger.getLogger(AuthService.class);

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String username, String plainPassword) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> {
                    LOG.warning("Login failed: username not found - " + username);
                    return new AuthenticationException("Invalid credentials");
                });

        if (!user.isActive()) {
            LOG.warning("Login failed: user is deactivated - " + username);
            throw new AuthenticationException("Invalid credentials");
        }

        if (!PasswordEncoder.matches(plainPassword, user.getPasswordHash())) {
            LOG.warning("Login failed: wrong password for - " + username);
            throw new AuthenticationException("Invalid credentials");
        }

        LOG.info("Login successful: " + user);
        return user;
    }
}
