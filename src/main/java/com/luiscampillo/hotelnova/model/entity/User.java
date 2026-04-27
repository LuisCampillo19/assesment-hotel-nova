package com.luiscampillo.hotelnova.model.entity;

import com.luiscampillo.hotelnova.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Hotel staff member able to log into the system.
 * The password field always holds a SHA-256 hash, never plain text.
 */
public class User {

    private int id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String fullName;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;

    public User() { }

    public User(int id, String username, String passwordHash, UserRole role,
                String fullName, String email, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id == u.id && Objects.equals(username, u.username);
    }

    @Override
    public int hashCode() { return Objects.hash(id, username); }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role
                + ", active=" + active + "}";
    }
}
