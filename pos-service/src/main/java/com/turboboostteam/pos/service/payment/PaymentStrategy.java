package com.turboboostteam.pos.service.payment;

import javax.money.MonetaryAmount;

// Strategy Pattern — Polymorphism
public interface PaymentStrategy {
    PaymentResult process(MonetaryAmount totalAmount,
                          MonetaryAmount amountTendered);
    String getPaymentType();
}