package com.turboboostteam.pos.model.sale;

import com.turboboostteam.pos.model.enums.PaymentType;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private Long saleId;
    private PaymentType paymentType;
    private MonetaryAmount amount;
    private String reference;
    private LocalDateTime createdAt;

    public Payment(Long saleId, PaymentType paymentType,
                   MonetaryAmount amount, String reference) {
        this.saleId      = saleId;
        this.paymentType = paymentType;
        this.amount      = amount;
        this.reference   = reference;
        this.createdAt   = LocalDateTime.now();
    }

    public Long getId()                  { return id; }
    public Long getSaleId()              { return saleId; }
    public PaymentType getPaymentType()  { return paymentType; }
    public MonetaryAmount getAmount()    { return amount; }
    public String getReference()         { return reference; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public void setId(Long id)           { this.id = id; }
}