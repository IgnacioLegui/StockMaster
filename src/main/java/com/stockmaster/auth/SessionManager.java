package com.stockmaster.auth;

import com.stockmaster.model.User;

/**
 * Singleton session manager. Holds the currently logged-in user
 * and provides role-checking utilities for the entire application.
 */
public final class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isCashier() {
        return currentUser != null && currentUser.isCashier();
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "—";
    }

    public String getCurrentDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : "—";
    }

    public String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : "—";
    }
}
