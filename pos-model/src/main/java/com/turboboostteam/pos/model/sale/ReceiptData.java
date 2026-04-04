package com.turboboostteam.pos.model.sale;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.List;

// Value object — holds all receipt data
public class ReceiptData {

    private final String saleNumber;
    private final String cashierName;
    private final LocalDateTime dateTime;
    private final List<SaleItem> items;
    private final MonetaryAmount subtotal;
    private final MonetaryAmount taxAmount;
    private final MonetaryAmount discountAmount;
    private final MonetaryAmount totalAmount;
    private final MonetaryAmount amountTendered;
    private final MonetaryAmount changeAmount;
    private final String paymentType;
    private final String paymentReference;
    private final int pointsEarned;
    private final int totalPoints;

    public ReceiptData(String saleNumber,
                       String cashierName,
                       LocalDateTime dateTime,
                       List<SaleItem> items,
                       MonetaryAmount subtotal,
                       MonetaryAmount taxAmount,
                       MonetaryAmount discountAmount,
                       MonetaryAmount totalAmount,
                       MonetaryAmount amountTendered,
                       MonetaryAmount changeAmount,
                       String paymentType,
                       String paymentReference,
                       int pointsEarned,
                       int totalPoints) {
        this.saleNumber       = saleNumber;
        this.cashierName      = cashierName;
        this.dateTime         = dateTime;
        this.items            = items;
        this.subtotal         = subtotal;
        this.taxAmount        = taxAmount;
        this.discountAmount   = discountAmount;
        this.totalAmount      = totalAmount;
        this.amountTendered   = amountTendered;
        this.changeAmount     = changeAmount;
        this.paymentType      = paymentType;
        this.paymentReference = paymentReference;
        this.pointsEarned     = pointsEarned;
        this.totalPoints      = totalPoints;
    }

    public String getSaleNumber()            { return saleNumber; }
    public String getCashierName()           { return cashierName; }
    public LocalDateTime getDateTime()       { return dateTime; }
    public List<SaleItem> getItems()         { return items; }
    public MonetaryAmount getSubtotal()      { return subtotal; }
    public MonetaryAmount getTaxAmount()     { return taxAmount; }
    public MonetaryAmount getDiscountAmount(){ return discountAmount; }
    public MonetaryAmount getTotalAmount()   { return totalAmount; }
    public MonetaryAmount getAmountTendered(){ return amountTendered; }
    public MonetaryAmount getChangeAmount()  { return changeAmount; }
    public String getPaymentType()           { return paymentType; }
    public String getPaymentReference()      { return paymentReference; }
    public int getPointsEarned()  { return pointsEarned; }
    public int getTotalPoints()   { return totalPoints; }
}