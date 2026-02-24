package com.stockmaster.controller;

import com.stockmaster.service.CategoryService;
import com.stockmaster.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CategoryController {

    @FXML private TableView<Category> tableCategories;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;

    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private Label lblMessage;

    private final CategoryService categoryService = new CategoryService();
    private Category selectedCategory;

    @FXML
    public void initialize() {
        setupColumns();
        loadTableData();

        tableCategories.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void loadTableData() {
        List<Category> categories = categoryService.findAll();
        tableCategories.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    public void handleClear() {
        selectedCategory = null;
        txtName.clear();
        txtDescription.clear();
        clearMessage();
        tableCategories.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleDelete() {
        Category selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a category to delete.");
            return;
        }
        try {
            categoryService.delete(selected.getId());
            loadTableData();
            handleClear();
            showSuccess("Category deleted.");
        } catch (Exception e) {
            showError("Error deleting: " + e.getMessage());
        }
    }

    @FXML
    public void handleSave() {
        if (txtName.getText().isEmpty()) {
            showError("Name is required.");
            return;
        }

        try {
            Category c = new Category();
            if (selectedCategory != null) {
                c.setId(selectedCategory.getId());
            }
            c.setName(txtName.getText());
            c.setDescription(txtDescription.getText());

            if (selectedCategory == null) {
                categoryService.insert(c);
                showSuccess("Category added.");
            } else {
                categoryService.update(c);
                showSuccess("Category updated.");
            }
            loadTableData();
            handleClear();
        } catch (Exception e) {
            showError("Error saving: " + e.getMessage());
        }
    }

    private void populateForm(Category c) {
        selectedCategory = c;
        txtName.setText(c.getName());
        txtDescription.setText(c.getDescription());
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
