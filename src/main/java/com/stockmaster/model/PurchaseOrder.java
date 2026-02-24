package com.stockmaster.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrder {
    private int id;
    private int supplierId;
    private String supplierName;
    private LocalDate orderDate;
    private String status; // PENDING, RECEIVED, CANCELLED
    private double total;
    private String notes;
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder() {
        this.orderDate = LocalDate.now();
        this.status = "PENDING";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<PurchaseOrderItem> getItems() { return items; }
    public void setItems(List<PurchaseOrderItem> items) { this.items = items; }

    public void recalculate() {
        total = 0;
        for (PurchaseOrderItem item : items) {
            item.setLineTotal(item.getUnitCost() * item.getQuantity());
            total += item.getLineTotal();
        }
    }
}
