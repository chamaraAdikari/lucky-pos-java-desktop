package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.CustomerDao;
import com.turboboostteam.pos.model.customer.Customer;
import com.turboboostteam.pos.model.customer.LoyaltyTier;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class LoyaltyService {

    private final CustomerDao customerDao;

    // 1 point = USD 0.01 redemption value
    private static final double POINT_VALUE = 0.01;

    // Max redemption = 20% of total
    private static final double MAX_REDEMPTION_PERCENT = 0.20;

    public LoyaltyService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    // ── Earn Points ──

    public int calculatePointsEarned(Customer customer,
                                     BigDecimal amount) {
        return customer.calculatePointsForPurchase(amount);
    }

    public void awardPoints(Long customerId,
                            BigDecimal purchaseAmount) {
        customerDao.findById(customerId).ifPresent(customer -> {
            int pointsEarned = customer
                    .calculatePointsForPurchase(purchaseAmount);

            int newPoints = customer.getLoyaltyPoints()
                    + pointsEarned;

            // Check tier upgrade
            LoyaltyTier oldTier = customer.getLoyaltyTier();
            LoyaltyTier newTier =
                    LoyaltyTier.fromPoints(newPoints);

            BigDecimal newTotal = customer.getTotalSpent()
                    .add(purchaseAmount);

            // Save to DB
            customerDao.updateLoyalty(
                    customerId, newPoints,
                    newTier.name(), newTotal);

            String username = SessionManager.getInstance()
                    .getCurrentUser().getUsername();

            AuditLogger.log("POINTS_AWARDED", username,
                    "customerId=" + customerId
                            + " points=" + pointsEarned
                            + " total=" + newPoints);

            // Log tier upgrade
            if (newTier != oldTier) {
                AuditLogger.log("TIER_UPGRADED", username,
                        "customerId=" + customerId
                                + " " + oldTier
                                + " → " + newTier);
            }
        });
    }

    // ── Redeem Points ──

    public MonetaryAmount calculateRedemptionValue(
            int points) {
        double value = points * POINT_VALUE;
        BigDecimal rounded = BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP);
        return Money.of(rounded, "USD");
    }

    public int calculateMaxRedeemablePoints(
            Customer customer,
            MonetaryAmount cartTotal) {
        double maxDiscount = cartTotal.getNumber()
                .doubleValue() * MAX_REDEMPTION_PERCENT;
        int maxPoints = (int) (maxDiscount / POINT_VALUE);
        return Math.min(maxPoints,
                customer.getLoyaltyPoints());
    }

    public Optional<RedemptionResult> redeemPoints(
            Long customerId,
            int pointsToRedeem,
            MonetaryAmount cartTotal) {

        Optional<Customer> customerOpt =
                customerDao.findById(customerId);
        if (customerOpt.isEmpty()) return Optional.empty();

        Customer customer = customerOpt.get();

        // Validate
        if (pointsToRedeem <= 0) {
            return Optional.of(RedemptionResult
                    .failure("Points must be positive"));
        }
        if (pointsToRedeem > customer.getLoyaltyPoints()) {
            return Optional.of(RedemptionResult
                    .failure("Insufficient points. Available: "
                            + customer.getLoyaltyPoints()));
        }

        int maxRedeemable = calculateMaxRedeemablePoints(
                customer, cartTotal);
        if (pointsToRedeem > maxRedeemable) {
            return Optional.of(RedemptionResult
                    .failure("Max redeemable: "
                            + maxRedeemable
                            + " points (20% of total)"));
        }

        MonetaryAmount discountValue =
                calculateRedemptionValue(pointsToRedeem);

        return Optional.of(RedemptionResult
                .success(pointsToRedeem,
                        discountValue, customer));
    }

    public void deductPoints(Long customerId,
                             int pointsRedeemed) {
        customerDao.findById(customerId).ifPresent(c -> {
            int newPoints = Math.max(0,
                    c.getLoyaltyPoints() - pointsRedeemed);
            LoyaltyTier newTier =
                    LoyaltyTier.fromPoints(newPoints);
            customerDao.updateLoyalty(
                    customerId, newPoints,
                    newTier.name(), c.getTotalSpent());

            AuditLogger.log("POINTS_REDEEMED",
                    SessionManager.getInstance()
                            .getCurrentUser().getUsername(),
                    "customerId=" + customerId
                            + " pointsRedeemed=" + pointsRedeemed
                            + " remaining=" + newPoints);
        });
    }

    // ── Result Types ──

    public static class RedemptionResult {
        private final boolean success;
        private final String message;
        private final int pointsRedeemed;
        private final MonetaryAmount discountValue;
        private final Customer customer;

        private RedemptionResult(boolean success,
                                 String message,
                                 int pointsRedeemed,
                                 MonetaryAmount discountValue,
                                 Customer customer) {
            this.success        = success;
            this.message        = message;
            this.pointsRedeemed = pointsRedeemed;
            this.discountValue  = discountValue;
            this.customer       = customer;
        }

        public static RedemptionResult success(
                int points,
                MonetaryAmount value,
                Customer customer) {
            return new RedemptionResult(true,
                    "Redeemed " + points + " points → "
                            + value + " off",
                    points, value, customer);
        }

        public static RedemptionResult failure(
                String reason) {
            return new RedemptionResult(false,
                    reason, 0, null, null);
        }

        public boolean isSuccess()          { return success; }
        public String getMessage()          { return message; }
        public int getPointsRedeemed()      { return pointsRedeemed; }
        public MonetaryAmount getDiscountValue() { return discountValue; }
        public Customer getCustomer()       { return customer; }
    }
}