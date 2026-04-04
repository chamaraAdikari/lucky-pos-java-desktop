package com.turboboostteam.pos.model.sale;

import com.turboboostteam.pos.model.enums.SaleStatus;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Encapsulation — state managed internally
public class SaleTransaction {

    private Long id;
    private String saleNumber;
    private Long cashierId;
    private SaleStatus status;
    private final List<SaleItem> items;
    private MonetaryAmount taxAmount;
    private MonetaryAmount discountAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private static final String CURRENCY = "USD";

    public SaleTransaction(Long cashierId) {
        this.cashierId      = cashierId;
        this.saleNumber     = generateSaleNumber();
        this.status         = SaleStatus.OPEN;
        this.items          = new ArrayList<>();
        this.taxAmount      = Money.of(0, CURRENCY);
        this.discountAmount = Money.of(0, CURRENCY);
        this.createdAt      = LocalDateTime.now();
    }

    // Full constructor for loading from DB
    public SaleTransaction(Long id, String saleNumber,
                           Long cashierId, SaleStatus status,
                           List<SaleItem> items,
                           MonetaryAmount taxAmount,
                           MonetaryAmount discountAmount,
                           LocalDateTime createdAt,
                           LocalDateTime completedAt) {
        this.id             = id;
        this.saleNumber     = saleNumber;
        this.cashierId      = cashierId;
        this.status         = status;
        this.items          = new ArrayList<>(items);
        this.taxAmount      = taxAmount;
        this.discountAmount = discountAmount;
        this.createdAt      = createdAt;
        this.completedAt    = completedAt;
    }

    // ---- Business Methods ----

    public void addItem(SaleItem item) {
        validateOpen();
        // If product already in cart — increase quantity
        items.stream()
                .filter(i -> i.getProductId()
                        .equals(item.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(
                                existing.getQuantity()
                                        + item.getQuantity()),
                        () -> items.add(item)
                );
    }

    public void removeItem(Long productId) {
        validateOpen();
        items.removeIf(i -> i.getProductId().equals(productId));
    }

    public void updateQuantity(Long productId, int quantity) {
        validateOpen();
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .ifPresent(i -> i.setQuantity(quantity));
    }

    public void commit() {
        validateOpen();
        this.status      = SaleStatus.COMMITTED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateOpen();
        this.status      = SaleStatus.VOIDED;
        this.completedAt = LocalDateTime.now();
    }

    // ---- Calculations ----

    public MonetaryAmount getSubtotal() {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(i.getQuantity()))
                .reduce(Money.of(0, CURRENCY),
                        MonetaryAmount::add);
    }

    public MonetaryAmount getTotalAmount() {
        return getSubtotal()
                .add(taxAmount)
                .subtract(discountAmount);
    }

    // ---- Discount ----
    public void applyDiscount(MonetaryAmount amount) {
        validateOpen();
        this.discountAmount = this.discountAmount.add(amount);
    }

    public void clearDiscount() {
        validateOpen();
        this.discountAmount = Money.of(0, CURRENCY);
    }

    // ---- Private ----

    private void validateOpen() {
        if (status != SaleStatus.OPEN) {
            throw new IllegalStateException(
                    "Cannot modify sale in state: " + status);
        }
    }

    private String generateSaleNumber() {
        return "SALE-" + System.currentTimeMillis();
    }

    // ---- Getters ----

    public void setId(Long id)              { this.id = id; }
    public void setTaxAmount(MonetaryAmount t)      { taxAmount = t; }
    public void setDiscountAmount(MonetaryAmount d) { discountAmount = d; }

    public Long getId()                      { return id; }
    public String getSaleNumber()            { return saleNumber; }
    public Long getCashierId()               { return cashierId; }
    public SaleStatus getStatus()            { return status; }
    public List<SaleItem> getItems()         { return Collections.unmodifiableList(items); }
    public MonetaryAmount getTaxAmount()     { return taxAmount; }
    public MonetaryAmount getDiscountAmount(){ return discountAmount; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getCompletedAt()    { return completedAt; }
    public boolean isEmpty()                 { return items.isEmpty(); }
    public int getItemCount()                { return items.size(); }


}