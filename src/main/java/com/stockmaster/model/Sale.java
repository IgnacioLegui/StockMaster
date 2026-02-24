package com.stockmaster.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private LocalDateTime saleDate;
    private int userId;
    private String username;
    private double subtotal;
    private double taxAmount;
    private double total;
    private String paymentMethod; // CASH, CARD, TRANSFER
    private String status; // COMPLETED, CANCELLED
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
        this.saleDate = LocalDateTime.now();
        this.paymentMethod = "CASH";
        this.status = "COMPLETED";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public void recalculate() {
        subtotal = 0;
        taxAmount = 0;
        for (SaleItem item : items) {
            double lineNet = item.getUnitPrice() * item.getQuantity();
            double lineTax = lineNet * (item.getTaxRate() / 100.0);
            item.setLineTotal(lineNet + lineTax);
            subtotal += lineNet;
            taxAmount += lineTax;
        }
        total = subtotal + taxAmount;
    }
}
