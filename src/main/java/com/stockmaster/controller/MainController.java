package com.stockmaster.controller;

import com.stockmaster.App;
import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.i18n.LanguageManager;
import com.stockmaster.service.BackupService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MainController {

    @FXML private BorderPane mainContainer;
    @FXML private Button btnDashboard, btnSales, btnProducts, btnPurchaseOrders;
    @FXML private Button btnCategories, btnSuppliers, btnReports, btnUsers;
    @FXML private Button btnBackup, btnRestore, btnLogout;
    @FXML private Label lblStatusProducts, lblUserInfo, lblVersion;
    @FXML private ComboBox<String> comboLanguage;

    private Object currentController;
    private String currentView = "Dashboard";

    @FXML
    public void initialize() {
        setupLanguageSelector();
        applyLanguageTexts();
        showDashboard();
        updateStatusBar();
        registerKeyboardShortcuts();
        applyRoleRestrictions();
    }

    /**
     * Apply i18n texts to all sidebar elements programmatically.
     */
    private void applyLanguageTexts() {
        LanguageManager lang = LanguageManager.getInstance();
        btnDashboard.setText("📊  " + lang.get("nav.dashboard"));
        btnSales.setText("💰  " + lang.get("nav.sales"));
        btnProducts.setText("📦  " + lang.get("nav.products"));
        btnPurchaseOrders.setText("📋  " + lang.get("nav.purchase_orders"));
        btnCategories.setText("🗂  " + lang.get("nav.categories"));
        btnSuppliers.setText("🚚  " + lang.get("nav.suppliers"));
        btnReports.setText("📈  " + lang.get("nav.reports"));
        btnUsers.setText("👥  " + lang.get("nav.users"));
        btnLogout.setText("🚪  " + lang.get("nav.logout"));
        btnBackup.setText("💾 " + lang.get("nav.backup"));
        btnRestore.setText("📂 " + lang.get("nav.restore"));
        lblVersion.setText(lang.get("status.version"));
    }

    private void setupLanguageSelector() {
        comboLanguage.setItems(FXCollections.observableArrayList("English", "Español"));
        
        LanguageManager lang = LanguageManager.getInstance();
        if (lang.isSpanish()) {
            comboLanguage.getSelectionModel().select("Español");
        } else {
            comboLanguage.getSelectionModel().select("English");
        }

        comboLanguage.setOnAction(e -> {
            String selected = comboLanguage.getValue();
            if ("Español".equals(selected)) {
                LanguageManager.getInstance().setLocale(new Locale("es"));
            } else {
                LanguageManager.getInstance().setLocale(new Locale("en"));
            }
            // Reload entire layout to apply language everywhere
            try {
                App.setRoot("MainLayout");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    // ==================== Navigation ====================

    @FXML
    private void showDashboard() {
        loadView("Dashboard");
        updateActiveButton(btnDashboard);
    }

    @FXML
    private void showSales() {
        loadView("SalesManagement");
        updateActiveButton(btnSales);
    }

    @FXML
    private void showProducts() {
        loadView("ProductManagement");
        updateActiveButton(btnProducts);
    }

    @FXML
    private void showPurchaseOrders() {
        if (!SessionManager.getInstance().isAdmin()) return;
        loadView("PurchaseOrders");
        updateActiveButton(btnPurchaseOrders);
    }

    @FXML
    private void showCategories() {
        if (!SessionManager.getInstance().isAdmin()) return;
        loadView("CategoryManagement");
        updateActiveButton(btnCategories);
    }

    @FXML
    private void showSuppliers() {
        if (!SessionManager.getInstance().isAdmin()) return;
        loadView("SupplierManagement");
        updateActiveButton(btnSuppliers);
    }

    @FXML
    private void showReports() {
        if (!SessionManager.getInstance().isAdmin()) return;
        loadView("Reports");
        updateActiveButton(btnReports);
    }

    @FXML
    private void showUsers() {
        if (!SessionManager.getInstance().isAdmin()) return;
        loadView("UserManagement");
        updateActiveButton(btnUsers);
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            App.setRoot("Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== Backup / Restore ====================

    @FXML
    private void handleBackup() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Backup Database");
        fc.setInitialFileName(BackupService.getDefaultBackupName());
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        File file = fc.showSaveDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            try {
                BackupService.backup(file);
                showAlert(Alert.AlertType.INFORMATION, "Backup", "Backup saved to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Backup Failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRestore() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Restore Database");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        File file = fc.showOpenDialog(mainContainer.getScene().getWindow());
        if (file != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "This will replace all current data.\nThe application will restart.\nContinue?", 
                ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Restore Database");
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    BackupService.restore(file);
                    showAlert(Alert.AlertType.INFORMATION, "Restore", "Database restored. Please restart the application.");
                    Platform.exit();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Restore Failed", e.getMessage());
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type, content);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    // ==================== UI Helpers ====================

    private void applyRoleRestrictions() {
        SessionManager session = SessionManager.getInstance();
        if (lblUserInfo != null) {
            lblUserInfo.setText("👤 " + session.getCurrentDisplayName() + " (" + session.getCurrentRole() + ")");
        }

        if (session.isCashier()) {
            Button[] adminOnly = {btnCategories, btnSuppliers, btnUsers, btnPurchaseOrders, btnReports};
            for (Button btn : adminOnly) {
                if (btn != null) {
                    btn.setDisable(true);
                    btn.setOpacity(0.4);
                }
            }
        }
    }

    private void updateActiveButton(Button activeButton) {
        Button[] allButtons = {btnDashboard, btnSales, btnProducts, btnPurchaseOrders, 
                               btnCategories, btnSuppliers, btnReports, btnUsers};
        for (Button btn : allButtons) {
            if (btn != null) btn.getStyleClass().remove("active");
        }
        if (activeButton != null) activeButton.getStyleClass().add("active");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
            loader.setResources(LanguageManager.getInstance().getBundle());
            Parent view = loader.load();
            currentController = loader.getController();
            currentView = fxml;
            mainContainer.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxml + " -> " + e.getMessage());
            if (e.getCause() != null) e.getCause().printStackTrace();
        }
    }

    private void updateStatusBar() {
        try {
            ProductDAO dao = new ProductDAO();
            int total = dao.findAll().size();
            LanguageManager lang = LanguageManager.getInstance();
            Platform.runLater(() -> lblStatusProducts.setText(lang.get("status.products") + ": " + total));
        } catch (Exception e) {
            // Silently fail for status bar
        }
    }

    // ==================== Keyboard Shortcuts ====================

    private void registerKeyboardShortcuts() {
        Platform.runLater(() -> {
            if (mainContainer.getScene() == null) return;

            mainContainer.getScene().setOnKeyPressed(event -> {
                if (new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.CONTROL_DOWN).match(event)) {
                    showDashboard(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.CONTROL_DOWN).match(event)) {
                    showSales(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.CONTROL_DOWN).match(event)) {
                    showProducts(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.CONTROL_DOWN).match(event)) {
                    showPurchaseOrders(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.CONTROL_DOWN).match(event)) {
                    showCategories(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.CONTROL_DOWN).match(event)) {
                    showSuppliers(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.CONTROL_DOWN).match(event)) {
                    showReports(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.CONTROL_DOWN).match(event)) {
                    showUsers(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
                    triggerSave(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                    triggerSearch(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                    triggerClear(); event.consume();
                }
                else if (new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN).match(event)) {
                    handleLogout(); event.consume();
                }
                else if (event.getCode() == KeyCode.DELETE && !event.isControlDown()) {
                    if (!(event.getTarget() instanceof TextInputControl)) {
                        triggerDelete(); event.consume();
                    }
                }
            });
        });
    }

    private void triggerSave() {
        if (currentController instanceof ProductController) ((ProductController) currentController).handleSave();
        else if (currentController instanceof CategoryController) ((CategoryController) currentController).handleSave();
        else if (currentController instanceof SupplierController) ((SupplierController) currentController).handleSave();
        else if (currentController instanceof UserController) ((UserController) currentController).handleSave();
        updateStatusBar();
    }

    private void triggerSearch() {
        if (currentController instanceof ProductController) ((ProductController) currentController).focusSearch();
    }

    private void triggerClear() {
        if (currentController instanceof ProductController) ((ProductController) currentController).handleClear();
        else if (currentController instanceof CategoryController) ((CategoryController) currentController).handleClear();
        else if (currentController instanceof SupplierController) ((SupplierController) currentController).handleClear();
        else if (currentController instanceof UserController) ((UserController) currentController).handleClear();
    }

    private void triggerDelete() {
        if (currentController instanceof ProductController) ((ProductController) currentController).handleDelete();
        else if (currentController instanceof CategoryController) ((CategoryController) currentController).handleDelete();
        else if (currentController instanceof SupplierController) ((SupplierController) currentController).handleDelete();
        else if (currentController instanceof UserController) ((UserController) currentController).handleDelete();
        updateStatusBar();
    }
}
