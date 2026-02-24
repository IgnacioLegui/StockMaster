package com.stockmaster.controller;

import com.stockmaster.auth.SessionManager;
import com.stockmaster.dao.ProductDAO;
import com.stockmaster.dao.PurchaseOrderDAO;
import com.stockmaster.dao.SupplierDAO;
import com.stockmaster.model.Product;
import com.stockmaster.model.PurchaseOrder;
import com.stockmaster.model.PurchaseOrderItem;
import com.stockmaster.model.Supplier;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderController {

    @FXML private TableView<PurchaseOrder> tableOrders;
    @FXML private TableColumn<PurchaseOrder, Number> colOrderId;
    @FXML private TableColumn<PurchaseOrder, String> colOrderDate;
    @FXML private TableColumn<PurchaseOrder, String> colOrderSupplier;
    @FXML private TableColumn<PurchaseOrder, Number> colOrderTotal;
    @FXML private TableColumn<PurchaseOrder, String> colOrderStatus;
    @FXML private TableColumn<PurchaseOrder, String> colOrderNotes;

    @FXML private VBox orderFormPanel;
    @FXML private ComboBox<Supplier> comboSupplier;
    @FXML private ComboBox<Product> comboProduct;
    @FXML private Spinner<Integer> spinnerOrderQty;
    @FXML private TextField txtUnitCost, txtNotes;
    @FXML private Label lblOrderTotal, lblMessage;

    @FXML private TableView<PurchaseOrderItem> tableOrderItems;
    @FXML private TableColumn<PurchaseOrderItem, String> colItemProduct;
    @FXML private TableColumn<PurchaseOrderItem, Number> colItemQty;
    @FXML private TableColumn<PurchaseOrderItem, Number> colItemCost;
    @FXML private TableColumn<PurchaseOrderItem, Number> colItemTotal;

    private final PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<PurchaseOrderItem> orderItems = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        setupOrderColumns();
        setupItemColumns();
        spinnerOrderQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99999, 1));
        tableOrderItems.setItems(orderItems);
        loadOrders();
    }

    private void setupOrderColumns() {
        colOrderId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colOrderDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getOrderDate() != null ? c.getValue().getOrderDate().format(DATE_FMT) : ""));
        colOrderSupplier.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getSupplierName() != null ? c.getValue().getSupplierName() : ""));
        colOrderTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()));
        colOrderStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colOrderNotes.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNotes() != null ? c.getValue().getNotes() : ""));

        // Color-code status
        colOrderStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item) {
                    case "PENDING" -> setStyle("-fx-text-fill: #ffa500;");
                    case "RECEIVED" -> setStyle("-fx-text-fill: #00d764;");
                    case "CANCELLED" -> setStyle("-fx-text-fill: #ff4d4d;");
                    default -> setStyle("");
                }
            }
        });
    }

    private void setupItemColumns() {
        colItemProduct.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProductName()));
        colItemQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        colItemCost.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getUnitCost()));
        colItemTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getLineTotal()));
    }

    @FXML
    private void handleNewOrder() {
        orderFormPanel.setVisible(true);
        orderFormPanel.setManaged(true);
        orderItems.clear();

        // Load combos
        List<Supplier> suppliers = supplierDAO.findAll();
        comboSupplier.setItems(FXCollections.observableArrayList(suppliers));

        List<Product> products = productDAO.findAll();
        comboProduct.setItems(FXCollections.observableArrayList(products));

        updateOrderTotal();
    }

    @FXML
    private void handleAddItem() {
        Product product = comboProduct.getValue();
        if (product == null) { showError("Select a product."); return; }

        double unitCost;
        try {
            unitCost = Double.parseDouble(txtUnitCost.getText().trim());
        } catch (NumberFormatException e) {
            unitCost = product.getPriceBuy(); // Default to buy price
        }

        int qty = spinnerOrderQty.getValue();
        PurchaseOrderItem item = new PurchaseOrderItem(product.getId(), product.getName(), qty, unitCost);
        orderItems.add(item);
        updateOrderTotal();
        txtUnitCost.clear();
    }

    @FXML
    private void handleSaveOrder() {
        Supplier supplier = comboSupplier.getValue();
        if (supplier == null) { showError("Select a supplier."); return; }
        if (orderItems.isEmpty()) { showError("Add at least one item."); return; }

        try {
            PurchaseOrder order = new PurchaseOrder();
            order.setSupplierId(supplier.getId());
            order.setNotes(txtNotes.getText().trim());
            order.setItems(new ArrayList<>(orderItems));
            order.recalculate();

            int orderId = poDAO.insert(order);
            showSuccess("Purchase Order #" + orderId + " created.");

            handleCancelForm();
            loadOrders();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleReceiveOrder() {
        PurchaseOrder selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order."); return; }
        if (!"PENDING".equals(selected.getStatus())) { showError("Only PENDING orders can be received."); return; }

        try {
            SessionManager session = SessionManager.getInstance();
            int userId = session.isLoggedIn() ? session.getCurrentUser().getId() : 0;
            poDAO.receive(selected.getId(), userId);
            showSuccess("Order #" + selected.getId() + " received. Stock updated.");
            loadOrders();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelOrder() {
        PurchaseOrder selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order."); return; }
        if (!"PENDING".equals(selected.getStatus())) { showError("Only PENDING orders can be cancelled."); return; }

        try {
            poDAO.cancel(selected.getId());
            showSuccess("Order #" + selected.getId() + " cancelled.");
            loadOrders();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewItems() {
        PurchaseOrder selected = tableOrders.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Select an order."); return; }

        List<PurchaseOrderItem> items = poDAO.findItemsByOrder(selected.getId());
        orderFormPanel.setVisible(true);
        orderFormPanel.setManaged(true);
        orderItems.clear();
        orderItems.addAll(items);
        updateOrderTotal();
    }

    @FXML
    private void handleCancelForm() {
        orderFormPanel.setVisible(false);
        orderFormPanel.setManaged(false);
        orderItems.clear();
        txtNotes.clear();
    }

    @FXML
    private void handleRefresh() {
        loadOrders();
    }

    private void loadOrders() {
        List<PurchaseOrder> orders = poDAO.findAll();
        tableOrders.setItems(FXCollections.observableArrayList(orders));
    }

    private void updateOrderTotal() {
        double total = orderItems.stream().mapToDouble(PurchaseOrderItem::getLineTotal).sum();
        lblOrderTotal.setText(String.format("$%.2f", total));
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
