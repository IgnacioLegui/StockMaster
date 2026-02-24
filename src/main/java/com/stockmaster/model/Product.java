package com.stockmaster.model;

public class Product {
    private int id;
    private String name;
    private String description;
    private int categoryId;
    private int supplierId;
    private double priceBuy;
    private double priceSell;
    private int stockCurrent;
    private int stockMin;
    private String barcode;
    private double taxRate = 21.0;

    // Helper fields for UI display (names instead of IDs)
    private String categoryName;
    private String supplierName;

    public Product() {}

    public Product(int id, String name, String description, int categoryId, int supplierId, 
                   double priceBuy, double priceSell, int stockCurrent, int stockMin) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.stockCurrent = stockCurrent;
        this.stockMin = stockMin;
    }

    public Product(String name, String description, int categoryId, int supplierId, 
                   double priceBuy, double priceSell, int stockCurrent, int stockMin) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.stockCurrent = stockCurrent;
        this.stockMin = stockMin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public double getPriceBuy() { return priceBuy; }
    public void setPriceBuy(double priceBuy) { this.priceBuy = priceBuy; }

    public double getPriceSell() { return priceSell; }
    public void setPriceSell(double priceSell) { this.priceSell = priceSell; }

    public int getStockCurrent() { return stockCurrent; }
    public void setStockCurrent(int stockCurrent) { this.stockCurrent = stockCurrent; }

    public int getStockMin() { return stockMin; }
    public void setStockMin(int stockMin) { this.stockMin = stockMin; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    @Override
    public String toString() { return name; }
}
