package com.turboboostteam.pos.service.payment;

import org.javamoney.moneta.Money;
import javax.money.MonetaryAmount;

// Concrete Strategy — Card
// Note: Stripe integration in Phase 6
// For now simulates card approval
public class CardPayment implements PaymentStrategy {

    @Override
    public PaymentResult process(MonetaryAmount totalAmount,
                                 MonetaryAmount amountTendered) {
        try {
            // Simulate card processing delay
            Thread.sleep(500);

            // Simulate 95% approval rate
            if (Math.random() < 0.95) {
                String reference = "CARD-"
                        + System.currentTimeMillis();
                // No change for card payments
                MonetaryAmount noChange =
                        Money.of(0, totalAmount
                                .getCurrency()
                                .getCurrencyCode());
                return PaymentResult.success(noChange, reference);
            } else {
                return PaymentResult.failure(
                        "Card declined — please try again");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.failure("Payment interrupted");
        }
    }

    @Override
    public String getPaymentType() { return "CARD"; }
}