package com.turboboostteam.pos.service.payment;

import org.javamoney.moneta.Money;
import javax.money.MonetaryAmount;

// Concrete Strategy — Cash
public class CashPayment implements PaymentStrategy {

    @Override
    public PaymentResult process(MonetaryAmount totalAmount,
                                 MonetaryAmount amountTendered) {
        // Validation
        if (amountTendered.isLessThan(totalAmount)) {
            return PaymentResult.failure(
                    "Insufficient cash. Need at least "
                            + totalAmount);
        }

        // Change calculation
        MonetaryAmount change = amountTendered
                .subtract(totalAmount);

        String reference = "CASH-"
                + System.currentTimeMillis();

        return PaymentResult.success(change, reference);
    }

    @Override
    public String getPaymentType() { return "CASH"; }
}