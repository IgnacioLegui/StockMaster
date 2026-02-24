package com.stockmaster.controller;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.dao.SaleDAO;
import com.stockmaster.hardware.BarcodeDetector;
import com.stockmaster.hardware.ReceiptPrinter;
import com.stockmaster.model.Product;
import com.stockmaster.model.Sale;
import com.stockmaster.model.SaleItem;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SalesController {

    // Search results table
    @FXML private TableView<Product> tableSearchResults;
    @FXML private TableColumn<Product, String> colSearchName;
    @FXML private TableColumn<Product, Number> colSearchPrice;
    @FXML private TableColumn<Product, Number> colSearchStock;
    @FXML private TableColumn<Product, Number> colSearchTax;

    // Cart table
    @FXML private TableView<SaleItem> tableCart;
    @FXML private TableColumn<SaleItem, String> colCartProduct;
    @FXML private TableColumn<SaleItem, Number> colCartQty;
    @FXML private TableColumn<SaleItem, Number> colCartPrice;
    @FXML private TableColumn<SaleItem, Number> colCartTax;
    @FXML private TableColumn<SaleItem, Number> colCartTotal;

    // Sales history table
    @FXML private TableView<Sale> tableSales;
    @FXML private TableColumn<Sale, Number> colSaleId;
    @FXML private TableColumn<Sale, String> colSaleDate;
    @FXML private TableColumn<Sale, String> colSaleUser;
    @FXML private TableColumn<Sale, Number> colSaleSubtotal;
    @FXML private TableColumn<Sale, Number> colSaleTax;
    @FXML private TableColumn<Sale, Number> colSaleTotal;
    @FXML private TableColumn<Sale, String> colSalePayment;
    @FXML private TableColumn<Sale, String> colSaleStatus;

    @FXML private TextField txtSearch;
    @FXML private Spinner<Integer> spinnerQty;
    @FXML private ComboBox<String> comboPayment;
    @FXML private Label lblSubtotal, lblTax, lblTotal, lblMessage;

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final ObservableList<SaleItem> cartItems = FXCollections.observableArrayList();
    private Sale currentSale;
    private Sale lastCompletedSale;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        setupSearchColumns();
        setupCartColumns();
        setupSalesColumns();

        spinnerQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        comboPayment.setItems(FXCollections.observableArrayList("CASH", "CARD", "TRANSFER"));
        comboPayment.setValue("CASH");

        tableCart.setItems(cartItems);
        currentSale = new Sale();

        // Barcode detection on search field
        new BarcodeDetector(this::onBarcodeScanned).attachTo(txtSearch);

        loadSalesHistory();
    }

    private void onBarcodeScanned(String barcode) {
        Product product = productDAO.findByBarcode(barcode);
        if (product != null) {
            addProductToCart(product, 1);
            showSuccess("Barcode: " + product.getName());
        } else {
            showError("Barcode not found: " + barcode);
        }
    }

    private void setupSearchColumns() {
        colSearchName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colSearchPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPriceSell()));
        colSearchStock.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getStockCurrent()));
        colSearchTax.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTaxRate()));

        // Double-click to add to cart
        tableSearchResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Product selected = tableSearchResults.getSelectionModel().getSelectedItem();
                if (selected != null) addProductToCart(selected, spinnerQty.getValue());
            }
        });
    }

    private void setupCartColumns() {
        colCartProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colCartQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        colCartPrice.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUnitPrice()));
        colCartTax.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTaxRate()));
        colCartTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getLineTotal()));
    }

    private void setupSalesColumns() {
        colSaleId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colSaleDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getSaleDate() != null ? c.getValue().getSaleDate().format(FMT) : ""));
        colSaleUser.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getUsername() != null ? c.getValue().getUsername() : ""));
        colSaleSubtotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getSubtotal()));
        colSaleTax.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTaxAmount()));
        colSaleTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));
        colSalePayment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMethod()));
        colSaleStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
    }

    @FXML
    private void handleProductSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;
        List<Product> results = productDAO.search(query);
        tableSearchResults.setItems(FXCollections.observableArrayList(results));
        if (results.isEmpty()) showError("No products found.");
    }

    private void addProductToCart(Product product, int qty) {
        if (product.getStockCurrent() < qty) {
            showError("Insufficient stock for " + product.getName());
            return;
        }

        // Check if already in cart
        for (SaleItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + qty);
                item.setLineTotal(item.getUnitPrice() * item.getQuantity() * (1 + item.getTaxRate() / 100.0));
                tableCart.refresh();
                updateTotals();
                return;
            }
        }

        SaleItem item = new SaleItem(product.getId(), product.getName(), qty, product.getPriceSell(), product.getTaxRate());
        cartItems.add(item);
        updateTotals();
    }

    @FXML
    private void handleAddToCart() {
        Product selected = tableSearchResults.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Select a product first.");
            return;
        }
        addProductToCart(selected, spinnerQty.getValue());
    }

    @FXML
    private void handleRemoveFromCart() {
        SaleItem selected = tableCart.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateTotals();
        }
    }

    @FXML
    private void handleClearCart() {
        cartItems.clear();
        updateTotals();
    }

    private void updateTotals() {
        currentSale.setItems(new java.util.ArrayList<>(cartItems));
        currentSale.recalculate();
        lblSubtotal.setText(String.format("$%.2f", currentSale.getSubtotal()));
        lblTax.setText(String.format("$%.2f", currentSale.getTaxAmount()));
        lblTotal.setText(String.format("$%.2f", currentSale.getTotal()));
    }

    @FXML
    private void handleCompleteSale() {
        if (cartItems.isEmpty()) {
            showError("Cart is empty.");
            return;
        }

        try {
            SessionManager session = SessionManager.getInstance();
            currentSale.setUserId(session.isLoggedIn() ? session.getCurrentUser().getId() : 0);
            currentSale.setPaymentMethod(comboPayment.getValue());
            currentSale.setItems(new java.util.ArrayList<>(cartItems));
            currentSale.recalculate();

            int saleId = saleDAO.insertSale(currentSale);
            lastCompletedSale = currentSale;
            lastCompletedSale.setId(saleId);

            showSuccess("Sale #" + saleId + " completed! Total: $" + String.format("%.2f", currentSale.getTotal()));

            // Reset
            cartItems.clear();
            currentSale = new Sale();
            updateTotals();
            loadSalesHistory();

            // Refresh search results to show updated stock
            handleProductSearch();

        } catch (Exception e) {
            showError("Sale failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrintReceipt() {
        if (lastCompletedSale == null) {
            showError("No recent sale to print.");
            return;
        }
        SessionManager session = SessionManager.getInstance();
        String cashier = session.isLoggedIn() ? session.getCurrentDisplayName() : "—";
        List<SaleItem> items = saleDAO.findItemsBySale(lastCompletedSale.getId());

        // Convert SaleItems to Products for receipt formatter
        java.util.List<Product> receiptProducts = new java.util.ArrayList<>();
        for (SaleItem item : items) {
            Product p = new Product();
            p.setName(item.getProductName());
            p.setPriceSell(item.getUnitPrice());
            p.setStockCurrent(item.getQuantity()); // reuse as qty for receipt
            receiptProducts.add(p);
        }

        String receipt = ReceiptPrinter.formatReceipt(receiptProducts, cashier, "StockMaster");
        if (!ReceiptPrinter.printToDefault(receipt)) {
            System.out.println(receipt); // Print to console if no printer
            showSuccess("Receipt printed to console (no printer found).");
        } else {
            showSuccess("Receipt sent to printer.");
        }
    }

    private void loadSalesHistory() {
        List<Sale> sales = saleDAO.findAll(100);
        tableSales.setItems(FXCollections.observableArrayList(sales));
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
