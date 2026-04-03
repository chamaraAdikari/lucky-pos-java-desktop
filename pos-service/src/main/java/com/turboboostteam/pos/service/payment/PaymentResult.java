package com.turboboostteam.pos.service.payment;

import javax.money.MonetaryAmount;

public class PaymentResult {

    private final boolean success;
    private final String  message;
    private final MonetaryAmount changeAmount;
    private final String  reference;

    public PaymentResult(boolean success, String message,
                         MonetaryAmount changeAmount,
                         String reference) {
        this.success      = success;
        this.message      = message;
        this.changeAmount = changeAmount;
        this.reference    = reference;
    }

    // Factory methods
    public static PaymentResult success(MonetaryAmount change,
                                        String reference) {
        return new PaymentResult(true, "Payment successful",
                change, reference);
    }

    public static PaymentResult failure(String reason) {
        return new PaymentResult(false, reason, null, null);
    }

    public boolean isSuccess()               { return success; }
    public String getMessage()               { return message; }
    public MonetaryAmount getChangeAmount()  { return changeAmount; }
    public String getReference()             { return reference; }
}