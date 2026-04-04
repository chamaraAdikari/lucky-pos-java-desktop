package com.turboboostteam.pos.model.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Builder Pattern — encapsulates PII fields
public class Customer {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private int loyaltyPoints;
    private LoyaltyTier loyaltyTier;
    private BigDecimal totalSpent;
    private final boolean active;
    private final LocalDateTime createdAt;

    // Private constructor — use Builder
    private Customer(Builder builder) {
        this.id            = builder.id;
        this.firstName     = builder.firstName;
        this.lastName      = builder.lastName;
        this.email         = builder.email;
        this.phone         = builder.phone;
        this.loyaltyPoints = builder.loyaltyPoints;
        this.loyaltyTier   = builder.loyaltyTier;
        this.totalSpent    = builder.totalSpent;
        this.active        = builder.active;
        this.createdAt     = builder.createdAt;
    }

    // ── Business Logic ──

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
        this.loyaltyTier =
                LoyaltyTier.fromPoints(this.loyaltyPoints);
    }

    public void addToTotalSpent(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
    }

    public int calculatePointsForPurchase(
            BigDecimal purchaseAmount) {
        return (int) (purchaseAmount.doubleValue()
                * loyaltyTier.getMultiplier());
    }

    // ── Getters ──
    public Long getId()               { return id; }
    public String getFirstName()      { return firstName; }
    public String getLastName()       { return lastName; }
    public String getEmail()          { return email; }
    public String getPhone()          { return phone; }
    public int getLoyaltyPoints()     { return loyaltyPoints; }
    public LoyaltyTier getLoyaltyTier() { return loyaltyTier; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public boolean isActive()         { return active; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    @Override
    public String toString() {
        return getFullName()
                + " [" + loyaltyTier + " — "
                + loyaltyPoints + " pts]";
    }

    // ── Builder ──
    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private int loyaltyPoints = 0;
        private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;
        private BigDecimal totalSpent = BigDecimal.ZERO;
        private boolean active = true;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id; return this;
        }
        public Builder firstName(String firstName) {
            this.firstName = firstName; return this;
        }
        public Builder lastName(String lastName) {
            this.lastName = lastName; return this;
        }
        public Builder email(String email) {
            this.email = email; return this;
        }
        public Builder phone(String phone) {
            this.phone = phone; return this;
        }
        public Builder loyaltyPoints(int points) {
            this.loyaltyPoints = points; return this;
        }
        public Builder loyaltyTier(LoyaltyTier tier) {
            this.loyaltyTier = tier; return this;
        }
        public Builder totalSpent(BigDecimal spent) {
            this.totalSpent = spent; return this;
        }
        public Builder active(boolean active) {
            this.active = active; return this;
        }
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt; return this;
        }
        public Customer build() {
            return new Customer(this);
        }
    }
}