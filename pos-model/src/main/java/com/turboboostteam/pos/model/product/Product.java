package com.turboboostteam.pos.model.product;

import javax.money.MonetaryAmount;

// Abstraction + Encapsulation
public abstract class Product {

    private Long id;
    private String name;
    private String barcode;
    private MonetaryAmount price;
    private Category category;
    private boolean active;

    // Constructor
    protected Product(Long id, String name, String barcode,
                      MonetaryAmount price, Category category, boolean active) {
        this.id       = id;
        this.name     = name;
        this.barcode  = barcode;
        this.price    = price;
        this.category = category;
        this.active   = active;
    }

    // Abstract methods — subclasses MUST implement
    public abstract String getProductType();
    public abstract MonetaryAmount getFinalPrice();

    // Getters — no setters (encapsulation)
    public Long getId()              { return id; }
    public String getName()          { return name; }
    public String getBarcode()       { return barcode; }
    public MonetaryAmount getPrice() { return price; }
    public Category getCategory()    { return category; }
    public boolean isActive()        { return active; }

    @Override
    public String toString() {
        return name + " [" + getProductType() + "] — " + price;
    }
}