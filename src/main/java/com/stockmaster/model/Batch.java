package com.stockmaster.model;

import java.time.LocalDate;

public class Batch {
    private int id;
    private int productId;
    private String lotNumber;
    private LocalDate expiryDate;
    private int quantity;
    private double costPrice;
    private LocalDate receivedDate;

    // Helper for display
    private String productName;

    public Batch() {}

    public Batch(int id, int productId, String lotNumber, LocalDate expiryDate,
                 int quantity, double costPrice, LocalDate receivedDate) {
        this.id = id;
        this.productId = productId;
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.costPrice = costPrice;
        this.receivedDate = receivedDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getLotNumber() { return lotNumber; }
    public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }

    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now().plusDays(days)) && !isExpired();
    }

    public String getExpiryStatus() {
        if (expiryDate == null) return "No Expiry";
        if (isExpired()) return "EXPIRED";
        if (isExpiringSoon(30)) return "Expiring Soon";
        return "OK";
    }

    @Override
    public String toString() {
        return lotNumber + " (qty: " + quantity + ")";
    }
}
