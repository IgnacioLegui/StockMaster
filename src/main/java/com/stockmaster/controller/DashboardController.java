package com.stockmaster.controller;

import com.stockmaster.dao.BatchDAO;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.model.Product;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblTotalProducts;
    @FXML private Label lblTotalValue;
    @FXML private Label lblLowStock;
    @FXML private Label lblExpiring;
    @FXML private BarChart<String, Number> chartCategories;

    private final ProductDAO productDAO = new ProductDAO();
    private final BatchDAO batchDAO = new BatchDAO();

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() {
                return productDAO.findAll();
            }
        };

        task.setOnSucceeded(e -> {
            List<Product> products = task.getValue();
            updateKPIs(products);
            updateChart(products);
        });
        
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            System.err.println("Error loading dashboard data: " + ex.getMessage());
        });

        new Thread(task).start();
    }

    private void updateKPIs(List<Product> products) {
        int totalProducts = products.size();
        
        double totalValue = products.stream()
                .mapToDouble(p -> p.getPriceBuy() * p.getStockCurrent())
                .sum();
        
        long lowStockCount = products.stream()
                .filter(p -> p.getStockCurrent() <= p.getStockMin())
                .count();

        // Batch expiry counts
        int expiringSoon = batchDAO.countExpiringSoon(30);
        int expired = batchDAO.countExpired();

        Platform.runLater(() -> {
            lblTotalProducts.setText(String.valueOf(totalProducts));
            lblTotalValue.setText(String.format("$%.2f", totalValue));
            lblLowStock.setText(String.valueOf(lowStockCount));
            
            if (expired > 0) {
                lblExpiring.setText(expiringSoon + " + " + expired + " expired");
                lblExpiring.setStyle("-fx-text-fill: #ff4d4d;");
            } else {
                lblExpiring.setText(String.valueOf(expiringSoon));
            }
        });
    }

    private void updateChart(List<Product> products) {
        Map<String, Long> categoryCounts = products.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategoryName() == null ? "Uncategorized" : p.getCategoryName(), 
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Products");
        
        for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        Platform.runLater(() -> {
            chartCategories.getData().clear();
            chartCategories.getData().add(series);
        });
    }
}
