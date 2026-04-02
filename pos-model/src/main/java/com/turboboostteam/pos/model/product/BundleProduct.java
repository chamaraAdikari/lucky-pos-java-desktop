package com.turboboostteam.pos.model.product;

import javax.money.MonetaryAmount;
import java.util.List;

// Inheritance — Bundle has multiple products inside
public class BundleProduct extends Product {

    private final List<Product> bundledItems;
    private final double discountPercent;

    public BundleProduct(Long id, String name, String barcode,
                         MonetaryAmount price, Category category,
                         boolean active, List<Product> bundledItems,
                         double discountPercent) {
        super(id, name, barcode, price, category, active);
        this.bundledItems    = bundledItems;
        this.discountPercent = discountPercent;
    }

    @Override
    public String getProductType() {
        return "BUNDLE";
    }

    @Override
    public MonetaryAmount getFinalPrice() {
        // Bundle price = declared price with discount already applied
        return getPrice();
    }

    public List<Product> getBundledItems()  { return bundledItems; }
    public double getDiscountPercent()      { return discountPercent; }
    public int getBundleSize()              { return bundledItems.size(); }
}