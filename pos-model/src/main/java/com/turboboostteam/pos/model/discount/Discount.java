package com.turboboostteam.pos.model.discount;

import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public class Discount {

    private Long id;
    private String code;
    private String name;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minPurchase;
    private boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    public Discount(Long id, String code, String name,
                    DiscountType discountType,
                    BigDecimal discountValue,
                    BigDecimal minPurchase,
                    boolean active,
                    LocalDateTime validFrom,
                    LocalDateTime validUntil) {
        this.id            = id;
        this.code          = code;
        this.name          = name;
        this.discountType  = discountType;
        this.discountValue = discountValue;
        this.minPurchase   = minPurchase;
        this.active        = active;
        this.validFrom     = validFrom;
        this.validUntil    = validUntil;
    }

    // Business logic — encapsulated
    public boolean isValid(MonetaryAmount cartTotal) {
        if (!active) return false;

        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom))
            return false;
        if (validUntil != null && now.isAfter(validUntil))
            return false;

        double total = cartTotal.getNumber().doubleValue();
        return total >= minPurchase.doubleValue();
    }

    // Calculate discount amount — Polymorphism via type
    public MonetaryAmount calculate(MonetaryAmount cartTotal) {
        double total = cartTotal.getNumber().doubleValue();

        double discountAmount = switch (discountType) {
            case PERCENTAGE ->
                    total * (discountValue.doubleValue() / 100);
            case FLAT ->
                    Math.min(discountValue.doubleValue(), total);
            case BUY_X_GET_Y ->
                    discountValue.doubleValue(); // fixed free item value
        };

        BigDecimal rounded = BigDecimal
                .valueOf(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return Money.of(rounded,
                cartTotal.getCurrency().getCurrencyCode());
    }

    // Getters
    public Long getId()                { return id; }
    public String getCode()            { return code; }
    public String getName()            { return name; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue()  { return discountValue; }
    public BigDecimal getMinPurchase()    { return minPurchase; }
    public boolean isActive()          { return active; }
    public LocalDateTime getValidFrom()  { return validFrom; }
    public LocalDateTime getValidUntil() { return validUntil; }

    @Override
    public String toString() { return name + " (" + code + ")"; }
}