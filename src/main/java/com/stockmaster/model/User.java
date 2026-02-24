package com.stockmaster.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // "ADMIN" or "CASHIER"
    private String displayName;
    private boolean active;

    public User() {}

    public User(int id, String username, String passwordHash, String role, String displayName, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }
    public boolean isCashier() { return "CASHIER".equalsIgnoreCase(role); }

    @Override
    public String toString() { return displayName != null ? displayName : username; }
}
