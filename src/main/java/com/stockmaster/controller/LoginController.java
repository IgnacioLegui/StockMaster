package com.stockmaster.controller;

import com.stockmaster.App;
import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.UserDAO;
import com.stockmaster.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Create default admin if no users exist (first-run setup)
        if (!userDAO.hasAnyUser()) {
            try {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(UserDAO.hashPassword("admin"));
                admin.setRole("ADMIN");
                admin.setDisplayName("Administrator");
                admin.setActive(true);
                userDAO.insert(admin);
                System.out.println("Default admin user created (admin/admin)");
            } catch (Exception e) {
                System.err.println("Error creating default admin: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);

        if (user != null) {
            // Login successful
            SessionManager.getInstance().login(user);
            try {
                App.setRoot("MainLayout");
            } catch (Exception e) {
                showError("Error loading main application: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Invalid credentials. Please try again.");
            txtPassword.clear();
            txtPassword.requestFocus();
        }
    }

    private void showError(String msg) {
        lblMessage.setText("✕ " + msg);
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
        lblMessage.getStyleClass().add("toast-error");
    }
}
