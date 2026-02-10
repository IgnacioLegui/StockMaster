package com.stockmaster.controller;

import com.stockmaster.dao.CategoryDAO;
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

    private final CategoryDAO categoryDAO = new CategoryDAO();
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
        List<Category> categories = categoryDAO.findAll();
        tableCategories.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void handleRefresh() {
        loadTableData();
        handleClear();
    }

    @FXML
    private void handleClear() {
        selectedCategory = null;
        txtName.clear();
        txtDescription.clear();
        lblMessage.setText("");
        tableCategories.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleDelete() {
        Category selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            lblMessage.setText("Select a category to delete.");
            return;
        }
        try {
            categoryDAO.delete(selected.getId());
            loadTableData();
            handleClear();
            lblMessage.setText("Category deleted.");
            lblMessage.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            lblMessage.setText("Error deleting: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty()) {
            lblMessage.setText("Name is required.");
            lblMessage.setStyle("-fx-text-fill: red;");
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
                categoryDAO.insert(c);
                lblMessage.setText("Category added.");
            } else {
                categoryDAO.update(c);
                lblMessage.setText("Category updated.");
            }
            lblMessage.setStyle("-fx-text-fill: green;");
            loadTableData();
            handleClear();
        } catch (Exception e) {
            lblMessage.setText("Error saving: " + e.getMessage());
            lblMessage.setStyle("-fx-text-fill: red;");
        }
    }

    private void populateForm(Category c) {
        selectedCategory = c;
        txtName.setText(c.getName());
        txtDescription.setText(c.getDescription());
    }
}
