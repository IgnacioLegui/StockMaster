package com.stockmaster.controller;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.UserDAO;
import com.stockmaster.model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colDisplayName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, Boolean> colActive;

    @FXML private TextField txtSearch;
    @FXML private TextField txtUsername;
    @FXML private TextField txtDisplayName;
    @FXML private ComboBox<String> comboRole;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkActive;
    @FXML private Label lblMessage;

    private final UserDAO userDAO = new UserDAO();
    private User selectedUser;

    @FXML
    public void initialize() {
        setupColumns();
        loadComboData();
        loadTableData();

        tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                populateForm(newSel);
            }
        });
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colDisplayName.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        // Color the active column
        colActive.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Yes" : "No");
                    setStyle(item ? "-fx-text-fill: #00d764;" : "-fx-text-fill: #ff4d4d;");
                }
            }
        });
    }

    private void loadComboData() {
        comboRole.setItems(FXCollections.observableArrayList("Admin", "Cashier"));
    }

    private void loadTableData() {
        List<User> users = userDAO.findAll();
        tableUsers.setItems(FXCollections.observableArrayList(users));
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadTableData();
        } else {
            List<User> filtered = userDAO.findAll().stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(query) ||
                                 u.getDisplayName().toLowerCase().contains(query) ||
                                 u.getRole().toLowerCase().contains(query))
                    .collect(Collectors.toList());
            tableUsers.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    public void handleClear() {
        selectedUser = null;
        txtUsername.clear();
        txtDisplayName.clear();
        txtPassword.clear();
        comboRole.getSelectionModel().clearSelection();
        chkActive.setSelected(true);
        clearMessage();
        tableUsers.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleSave() {
        String username = txtUsername.getText().trim();
        String displayName = txtDisplayName.getText().trim();
        String role = comboRole.getValue();
        String password = txtPassword.getText();

        if (username.isEmpty() || displayName.isEmpty() || role == null) {
            showError("Username, display name, and role are required.");
            return;
        }

        try {
            if (selectedUser == null) {
                // New user — password required
                if (password.isEmpty()) {
                    showError("Password is required for new users.");
                    return;
                }
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPasswordHash(UserDAO.hashPassword(password));
                newUser.setDisplayName(displayName);
                newUser.setRole(role);
                newUser.setActive(chkActive.isSelected());
                userDAO.insert(newUser);
                showSuccess("User added successfully.");
            } else {
                // Update existing
                selectedUser.setUsername(username);
                selectedUser.setDisplayName(displayName);
                selectedUser.setRole(role);
                selectedUser.setActive(chkActive.isSelected());
                userDAO.update(selectedUser);

                // Update password only if provided
                if (!password.isEmpty()) {
                    userDAO.updatePassword(selectedUser.getId(), password);
                }
                showSuccess("User updated successfully.");
            }
            loadTableData();
            handleClear();
        } catch (Exception e) {
            showError("Error saving: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDelete() {
        User selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a user to delete.");
            return;
        }

        // Prevent self-deletion
        SessionManager session = SessionManager.getInstance();
        if (selected.getUsername().equals(session.getCurrentUser().getUsername())) {
            showError("Cannot delete your own account.");
            return;
        }

        try {
            userDAO.delete(selected.getId());
            loadTableData();
            handleClear();
            showSuccess("User deleted.");
        } catch (Exception e) {
            showError("Error deleting: " + e.getMessage());
        }
    }

    private void populateForm(User u) {
        selectedUser = u;
        txtUsername.setText(u.getUsername());
        txtDisplayName.setText(u.getDisplayName());
        comboRole.setValue(u.getRole());
        chkActive.setSelected(u.isActive());
        txtPassword.clear(); // Never show existing password
    }

    // ==================== Toast-style Feedback ====================
    private void showSuccess(String msg) {
        lblMessage.setText("✓ " + msg);
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
        lblMessage.getStyleClass().add("toast-success");
    }

    private void showError(String msg) {
        lblMessage.setText("✕ " + msg);
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
        lblMessage.getStyleClass().add("toast-error");
    }

    private void clearMessage() {
        lblMessage.setText("");
        lblMessage.getStyleClass().removeAll("toast-error", "toast-success");
    }
}
