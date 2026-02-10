package com.stockmaster.controller;

import com.stockmaster.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML private javafx.scene.control.Button btnDashboard;
    @FXML private javafx.scene.control.Button btnProducts;
    @FXML private javafx.scene.control.Button btnCategories;
    @FXML private javafx.scene.control.Button btnSuppliers;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        loadView("Dashboard");
        updateActiveButton(btnDashboard);
    }

    @FXML
    private void showProducts() {
        loadView("ProductManagement");
        updateActiveButton(btnProducts);
    }

    @FXML
    private void showCategories() {
        loadView("CategoryManagement");
        updateActiveButton(btnCategories);
    }

    @FXML
    private void showSuppliers() {
        loadView("SupplierManagement");
        updateActiveButton(btnSuppliers);
    }

    private void updateActiveButton(javafx.scene.control.Button activeButton) {
        btnDashboard.getStyleClass().remove("active");
        btnProducts.getStyleClass().remove("active");
        btnCategories.getStyleClass().remove("active");
        btnSuppliers.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
            Parent view = loader.load();
            mainContainer.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxml);
        }
    }
}
