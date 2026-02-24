package com.stockmaster.controller;

import com.stockmaster.service.SupplierService;
import com.stockmaster.model.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SupplierController {

    @FXML private TableView<Supplier> tableSuppliers;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colEmail;

    @FXML private TextField txtName;
    @FXML private TextField txtContact;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;

    private final SupplierService supplierService = new SupplierService();
    private Supplier selectedSupplier;

    @FXML
    public void initialize() {
        setupColumns();
        loadTableData();

        tableSuppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadTableData() {
        List<Supplier> suppliers = supplierService.findAll();
        tableSuppliers.setItems(FXCollections.observableArrayList(suppliers));
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    public void handleClear() {
        selectedSupplier = null;
        txtName.clear();
        txtContact.clear();
        txtPhone.clear();
        txtEmail.clear();
        clearMessage();
        tableSuppliers.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleDelete() {
        Supplier selected = tableSuppliers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a supplier to delete.");
            return;
        }
        try {
            supplierService.delete(selected.getId());
            loadTableData();
            handleClear();
            showSuccess("Supplier deleted.");
        } catch (Exception e) {
            showError("Error deleting: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (txtName.getText().isEmpty()) {
            showError("Company Name is required.");
            return;
        }
        
        String email = txtEmail.getText();
        if (email != null && !email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
             showError("Invalid Email format.");
             return;
        }
        
        String phone = txtPhone.getText();
        if (phone != null && !phone.isEmpty() && !phone.matches("\\d{3}-\\d{4}|\\d+")) {
             showError("Invalid Phone format (e.g. 555-1234 or 123456789).");
             return;
        }

        try {
            Supplier s = new Supplier();
            if (selectedSupplier != null) {
                s.setId(selectedSupplier.getId());
            }
            s.setName(txtName.getText());
            s.setContactName(txtContact.getText());
            s.setPhone(txtPhone.getText());
            s.setEmail(txtEmail.getText());

            if (selectedSupplier == null) {
                supplierService.insert(s);
                showSuccess("Supplier added.");
            } else {
                supplierService.update(s);
                showSuccess("Supplier updated.");
            }
            loadTableData();
            handleClear();
        } catch (Exception e) {
            showError("Error saving: " + e.getMessage());
        }
    }

    private void populateForm(Supplier s) {
        selectedSupplier = s;
        txtName.setText(s.getName());
        txtContact.setText(s.getContactName());
        txtPhone.setText(s.getPhone());
        txtEmail.setText(s.getEmail());
    }

    // Toast-style feedback
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
