package com.turboboostteam.pos.model.tax;

// Drools fact object — must be a simple POJO
public class TaxFact {

    private String  category;
    private double  unitPrice;
    private int     quantity;
    private double  taxRate;    // set BY the rule
    private double  taxAmount;  // calculated after rules fire

    public TaxFact(String category, double unitPrice,
                   int quantity) {
        this.category  = category;
        this.unitPrice = unitPrice;
        this.quantity  = quantity;
        this.taxRate   = 0.0;
        this.taxAmount = 0.0;
    }

    // Drools sets these via setters
    public void setTaxRate(double taxRate) {
        this.taxRate   = taxRate;
        this.taxAmount = unitPrice * quantity * taxRate;
    }

    public String getCategory()  { return category; }
    public double getUnitPrice() { return unitPrice; }
    public int    getQuantity()  { return quantity; }
    public double getTaxRate()   { return taxRate; }
    public double getTaxAmount() { return taxAmount; }
}