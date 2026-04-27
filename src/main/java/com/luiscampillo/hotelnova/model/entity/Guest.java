package com.luiscampillo.hotelnova.model.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Customer who stays at the hotel. Distinct from User (a Guest never logs in).
 * The "active" flag enables soft-deactivation without losing reservation history.
 */
public class Guest {

    private int id;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;

    public Guest() { }

    public Guest(int id, String documentNumber, String firstName, String lastName,
                 String phone, String email, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
    }

    /** Convenience method - "First Last" for display purposes. */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest g)) return false;
        return id == g.id && Objects.equals(documentNumber, g.documentNumber);
    }

    @Override
    public int hashCode() { return Objects.hash(id, documentNumber); }

    @Override
    public String toString() {
        return "Guest{id=" + id + ", document='" + documentNumber + "', name='"
                + getFullName() + "', active=" + active + "}";
    }
}
