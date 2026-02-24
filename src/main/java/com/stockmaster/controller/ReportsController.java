package com.stockmaster.controller;

import com.stockmaster.dao.*;
import com.stockmaster.model.Product;
import com.stockmaster.model.StockMovement;
import com.stockmaster.service.ExcelExporter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML private ComboBox<String> comboReportType;
    @FXML private DatePicker dateFrom, dateTo;
    @FXML private TableView<String[]> tableReport;
    @FXML private Label lblSummary1, lblSummary2, lblSummary3, lblMessage;

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final StockMovementDAO movementDAO = new StockMovementDAO();
    private final BatchDAO batchDAO = new BatchDAO();

    private String[] currentHeaders;
    private List<String[]> currentData;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        comboReportType.setItems(FXCollections.observableArrayList(
            "Inventory Value",
            "Profit Margins",
            "Sales Summary",
            "Top Products",
            "Stock Movements",
            "Low Stock",
            "Expiring Soon"
        ));
        comboReportType.setValue("Inventory Value");

        dateFrom.setValue(LocalDate.now().minusMonths(1));
        dateTo.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerate() {
        String type = comboReportType.getValue();
        if (type == null) return;

        switch (type) {
            case "Inventory Value" -> generateInventoryValue();
            case "Profit Margins" -> generateProfitMargins();
            case "Sales Summary" -> generateSalesSummary();
            case "Top Products" -> generateTopProducts();
            case "Stock Movements" -> generateStockMovements();
            case "Low Stock" -> generateLowStock();
            case "Expiring Soon" -> generateExpiringSoon();
        }
    }

    private void generateInventoryValue() {
        List<Product> products = productDAO.findAll();
        currentHeaders = new String[]{"Product", "Category", "Buy Price", "Stock", "Value"};
        currentData = new ArrayList<>();
        double totalValue = 0;

        for (Product p : products) {
            double value = p.getPriceBuy() * p.getStockCurrent();
            totalValue += value;
            currentData.add(new String[]{
                p.getName(),
                p.getCategoryName() != null ? p.getCategoryName() : "—",
                String.format("%.2f", p.getPriceBuy()),
                String.valueOf(p.getStockCurrent()),
                String.format("%.2f", value)
            });
        }

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(products.size()));
        lblSummary2.setText(String.format("$%.2f", totalValue));
        lblSummary3.setText(String.format("$%.2f", products.isEmpty() ? 0 : totalValue / products.size()));
    }

    private void generateProfitMargins() {
        List<Product> products = productDAO.findAll();
        currentHeaders = new String[]{"Product", "Buy", "Sell", "Margin", "Margin %"};
        currentData = new ArrayList<>();

        for (Product p : products) {
            double margin = p.getPriceSell() - p.getPriceBuy();
            double marginPct = p.getPriceBuy() > 0 ? (margin / p.getPriceBuy()) * 100 : 0;
            currentData.add(new String[]{
                p.getName(),
                String.format("%.2f", p.getPriceBuy()),
                String.format("%.2f", p.getPriceSell()),
                String.format("%.2f", margin),
                String.format("%.1f%%", marginPct)
            });
        }

        double avgMargin = currentData.stream()
            .mapToDouble(r -> Double.parseDouble(r[3]))
            .average().orElse(0);

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(products.size()));
        lblSummary2.setText(String.format("$%.2f", currentData.stream().mapToDouble(r -> Double.parseDouble(r[3])).sum()));
        lblSummary3.setText(String.format("$%.2f", avgMargin));
    }

    private void generateSalesSummary() {
        String from = dateFrom.getValue().format(DATE_FMT);
        String to = dateTo.getValue().format(DATE_FMT);
        double[] summary = saleDAO.getSalesSummary(from, to);

        var sales = saleDAO.findByDateRange(from, to);
        currentHeaders = new String[]{"ID", "Date", "Subtotal", "Tax", "Total", "Payment", "Status"};
        currentData = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (var s : sales) {
            currentData.add(new String[]{
                String.valueOf(s.getId()),
                s.getSaleDate() != null ? s.getSaleDate().format(fmt) : "",
                String.format("%.2f", s.getSubtotal()),
                String.format("%.2f", s.getTaxAmount()),
                String.format("%.2f", s.getTotal()),
                s.getPaymentMethod(),
                s.getStatus()
            });
        }

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf((int) summary[0]));
        lblSummary2.setText(String.format("$%.2f", summary[1]));
        lblSummary3.setText(String.format("$%.2f", summary[2]));
    }

    private void generateTopProducts() {
        List<String[]> top = saleDAO.getTopProducts(20);
        currentHeaders = new String[]{"Product", "Total Qty Sold", "Total Revenue"};
        currentData = top;

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(top.size()));
        double totalRev = top.stream().mapToDouble(r -> Double.parseDouble(r[2])).sum();
        lblSummary2.setText(String.format("$%.2f", totalRev));
        lblSummary3.setText("—");
    }

    private void generateStockMovements() {
        String from = dateFrom.getValue().format(DATE_FMT);
        String to = dateTo.getValue().format(DATE_FMT);
        List<StockMovement> movements = movementDAO.findByDateRange(from, to);
        currentHeaders = new String[]{"Date", "Product", "Type", "Qty", "Reason", "User"};
        currentData = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (StockMovement m : movements) {
            currentData.add(new String[]{
                m.getTimestamp() != null ? m.getTimestamp().format(fmt) : "",
                m.getProductName() != null ? m.getProductName() : "—",
                m.getType(),
                String.valueOf(m.getQuantity()),
                m.getReason() != null ? m.getReason() : "",
                m.getUsername() != null ? m.getUsername() : "—"
            });
        }

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(movements.size()));
        long inCount = movements.stream().filter(m -> "IN".equals(m.getType())).count();
        long outCount = movements.stream().filter(m -> "OUT".equals(m.getType())).count();
        lblSummary2.setText("IN: " + inCount);
        lblSummary3.setText("OUT: " + outCount);
    }

    private void generateLowStock() {
        List<Product> products = productDAO.findAll().stream()
            .filter(p -> p.getStockCurrent() <= p.getStockMin())
            .collect(Collectors.toList());

        currentHeaders = new String[]{"Product", "Category", "Current Stock", "Minimum", "Deficit"};
        currentData = new ArrayList<>();

        for (Product p : products) {
            currentData.add(new String[]{
                p.getName(),
                p.getCategoryName() != null ? p.getCategoryName() : "—",
                String.valueOf(p.getStockCurrent()),
                String.valueOf(p.getStockMin()),
                String.valueOf(p.getStockMin() - p.getStockCurrent())
            });
        }

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(products.size()));
        lblSummary2.setText("⚠ Alert");
        lblSummary3.setText("—");
    }

    private void generateExpiringSoon() {
        var batches = batchDAO.findExpiringSoon(30);
        currentHeaders = new String[]{"Product", "Lot #", "Expiry Date", "Qty", "Status"};
        currentData = new ArrayList<>();

        for (var b : batches) {
            currentData.add(new String[]{
                b.getProductName() != null ? b.getProductName() : "—",
                b.getLotNumber() != null ? b.getLotNumber() : "—",
                b.getExpiryDate() != null ? b.getExpiryDate().toString() : "—",
                String.valueOf(b.getQuantity()),
                b.getExpiryStatus()
            });
        }

        buildTable(currentHeaders, currentData);
        lblSummary1.setText(String.valueOf(batches.size()));
        lblSummary2.setText("⚠ Expiring");
        lblSummary3.setText("—");
    }

    @SuppressWarnings("unchecked")
    private void buildTable(String[] headers, List<String[]> data) {
        tableReport.getColumns().clear();
        for (int i = 0; i < headers.length; i++) {
            final int col = i;
            TableColumn<String[], String> column = new TableColumn<>(headers[i]);
            column.setCellValueFactory(c -> {
                String[] row = c.getValue();
                return new SimpleStringProperty(col < row.length ? row[col] : "");
            });
            column.setPrefWidth(120);
            tableReport.getColumns().add(column);
        }
        tableReport.setItems(FXCollections.observableArrayList(data));
    }

    // ==================== Export ====================

    @FXML
    private void handleExportPDF() {
        if (currentData == null || currentData.isEmpty()) { showError("Generate a report first."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Export PDF");
        fc.setInitialFileName("report.pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File file = fc.showSaveDialog(tableReport.getScene().getWindow());
        if (file == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.beginText();
            cs.newLineAtOffset(50, 780);
            cs.showText("StockMaster Report — " + comboReportType.getValue());
            cs.endText();

            cs.setFont(PDType1Font.HELVETICA, 9);
            float y = 750;
            float margin = 50;

            // Headers
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            for (String h : currentHeaders) {
                cs.showText(String.format("%-18s", h.length() > 16 ? h.substring(0, 16) : h));
            }
            cs.endText();
            y -= 15;

            // Data
            for (String[] row : currentData) {
                if (y < 50) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.setFont(PDType1Font.HELVETICA, 9);
                    y = 780;
                }
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                for (String cell : row) {
                    String val = cell != null ? cell : "";
                    if (val.length() > 16) val = val.substring(0, 16);
                    cs.showText(String.format("%-18s", val));
                }
                cs.endText();
                y -= 12;
            }
            cs.close();
            doc.save(file);
            showSuccess("PDF exported: " + file.getName());
        } catch (Exception e) {
            showError("PDF export failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportExcel() {
        if (currentData == null || currentData.isEmpty()) { showError("Generate a report first."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Export Excel");
        fc.setInitialFileName("report.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = fc.showSaveDialog(tableReport.getScene().getWindow());
        if (file == null) return;

        try {
            ExcelExporter.export(file, comboReportType.getValue(), currentHeaders, currentData);
            showSuccess("Excel exported: " + file.getName());
        } catch (Exception e) {
            showError("Excel export failed: " + e.getMessage());
        }
    }

    // ==================== Toast Feedback ====================
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
}
