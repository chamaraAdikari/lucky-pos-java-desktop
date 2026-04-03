package com.turboboostteam.pos.model.sale;

import javax.money.MonetaryAmount;

// Encapsulation — all fields private
public class SaleItem {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private MonetaryAmount unitPrice;
    private MonetaryAmount taxAmount;
    private MonetaryAmount discountAmount;

    public SaleItem(Long productId, String productName,
                    int quantity, MonetaryAmount unitPrice) {
        this.productId      = productId;
        this.productName    = productName;
        this.quantity       = quantity;
        this.unitPrice      = unitPrice;
        this.taxAmount      = unitPrice.multiply(0); // zero initially
        this.discountAmount = unitPrice.multiply(0); // zero initially
    }

    // Business logic — encapsulated
    public MonetaryAmount getLineTotal() {
        return unitPrice
                .multiply(quantity)
                .add(taxAmount)
                .subtract(discountAmount);
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTaxAmount(MonetaryAmount taxAmount) {
        this.taxAmount = taxAmount;
    }

    public void setDiscountAmount(MonetaryAmount discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setId(Long id) { this.id = id; }

    // Getters
    public Long getId()                      { return id; }
    public Long getProductId()               { return productId; }
    public String getProductName()           { return productName; }
    public int getQuantity()                 { return quantity; }
    public MonetaryAmount getUnitPrice()     { return unitPrice; }
    public MonetaryAmount getTaxAmount()     { return taxAmount; }
    public MonetaryAmount getDiscountAmount(){ return discountAmount; }
}