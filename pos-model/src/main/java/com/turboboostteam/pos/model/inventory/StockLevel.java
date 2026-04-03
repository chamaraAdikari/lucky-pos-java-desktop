package com.turboboostteam.pos.model.inventory;

public class StockLevel {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private int lowStockThreshold;

    public StockLevel(Long id, Long productId, String productName,
                      int quantity, int lowStockThreshold) {
        this.id                = id;
        this.productId         = productId;
        this.productName       = productName;
        this.quantity          = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    // Business logic — encapsulated
    public boolean isLowStock()   { return quantity <= lowStockThreshold; }
    public boolean isOutOfStock() { return quantity <= 0; }

    public Long getId()                  { return id; }
    public Long getProductId()           { return productId; }
    public String getProductName()       { return productName; }
    public int getQuantity()             { return quantity; }
    public int getLowStockThreshold()    { return lowStockThreshold; }
}