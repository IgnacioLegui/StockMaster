package com.stockmaster.controller;

import com.stockmaster.dao.SupplierDAO;
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

    private final SupplierDAO supplierDAO = new SupplierDAO();
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
        List<Supplier> suppliers = supplierDAO.findAll();
        tableSuppliers.setItems(FXCollections.observableArrayList(suppliers));
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    private void handleClear() {
        selectedSupplier = null;
        txtName.clear();
        txtContact.clear();
        txtPhone.clear();
        txtEmail.clear();
        lblMessage.setText("");
        tableSuppliers.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDelete() {
        Supplier selected = tableSuppliers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Select a supplier to delete.");
            return;
        }
        try {
            supplierDAO.delete(selected.getId());
            loadTableData();
            handleClear();
            lblMessage.setText("Supplier deleted.");
            lblMessage.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblMessage.setText("Error deleting: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty()) {
            lblMessage.setText("Company Name is required.");
            lblMessage.setStyle("-fx-text-fill: red;");
            return;
        }
        
        String email = txtEmail.getText();
        if (email != null && !email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
             lblMessage.setText("Invalid Email format.");
             lblMessage.setStyle("-fx-text-fill: red;");
             return;
        }
        
        String phone = txtPhone.getText();
        if (phone != null && !phone.isEmpty() && !phone.matches("\\d{3}-\\d{4}|\\d+")) {
             lblMessage.setText("Invalid Phone format (e.g. 555-1234 or 123456789).");
             lblMessage.setStyle("-fx-text-fill: red;");
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
                supplierDAO.insert(s);
                lblMessage.setText("Supplier added.");
            } else {
                supplierDAO.update(s);
                lblMessage.setText("Supplier updated.");
            }
            lblMessage.setStyle("-fx-text-fill: green;");
            loadTableData();
            handleClear();
        } catch (Exception e) {
            lblMessage.setText("Error saving: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void populateForm(Supplier s) {
        selectedSupplier = s;
        txtName.setText(s.getName());
        txtContact.setText(s.getContactName());
        txtPhone.setText(s.getPhone());
        txtEmail.setText(s.getEmail());
    }
}
