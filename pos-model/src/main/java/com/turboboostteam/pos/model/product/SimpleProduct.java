package com.turboboostteam.pos.model.product;

import javax.money.MonetaryAmount;

// Inheritance from Product
public class SimpleProduct extends Product {

    public SimpleProduct(Long id, String name, String barcode,
                         MonetaryAmount price, Category category, boolean active) {
        super(id, name, barcode, price, category, active);
    }

    @Override
    public String getProductType() {
        return "SIMPLE";
    }

    @Override
    public MonetaryAmount getFinalPrice() {
        return getPrice(); // Simple product — price as is
    }
}