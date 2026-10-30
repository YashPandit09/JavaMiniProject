package com.rental.model;

import java.io.Serializable;

/**
 * A staff login (one row of the users table).
 * Serializable because the web app keeps it in the HttpSession.
 */
public class User implements Serializable {

    private int userId;
    private String username;
    private String role;          // ADMIN or STAFF

    public User() {
    }

    public User(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /** Only ADMIN can add or edit vehicles. */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
