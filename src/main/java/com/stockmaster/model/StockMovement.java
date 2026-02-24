package com.stockmaster.model;

import java.time.LocalDateTime;

public class StockMovement {
    private int id;
    private int productId;
    private String productName;
    private String type; // IN, OUT, ADJUSTMENT
    private int quantity;
    private String reason;
    private int userId;
    private String username;
    private LocalDateTime timestamp;

    public StockMovement() {}

    public StockMovement(int productId, String type, int quantity, String reason, int userId) {
        this.productId = productId;
        this.type = type;
        this.quantity = quantity;
        this.reason = reason;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
