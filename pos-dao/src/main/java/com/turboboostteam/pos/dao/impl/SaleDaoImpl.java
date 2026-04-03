package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.SaleDao;
import com.turboboostteam.pos.model.enums.SaleStatus;
import com.turboboostteam.pos.model.enums.PaymentType;
import com.turboboostteam.pos.model.sale.*;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.*;

public class SaleDaoImpl implements SaleDao {

    private final DSLContext dsl;

    public SaleDaoImpl(DSLContext dsl) { this.dsl = dsl; }

    @Override
    public Long saveSale(SaleTransaction sale) {
        return dsl.insertInto(SALES)
                .set(SALES.SALE_NUMBER, sale.getSaleNumber())
                .set(SALES.CASHIER_ID, sale.getCashierId())
                .set(SALES.STATUS, sale.getStatus().name())
                .set(SALES.SUBTOTAL,
                        toBigDecimal(sale.getSubtotal()))
                .set(SALES.TAX_AMOUNT,
                        toBigDecimal(sale.getTaxAmount()))
                .set(SALES.DISCOUNT_AMOUNT,
                        toBigDecimal(sale.getDiscountAmount()))
                .set(SALES.TOTAL_AMOUNT,
                        toBigDecimal(sale.getTotalAmount()))
                .set(SALES.CURRENCY, "USD")
                .returningResult(SALES.ID)
                .fetchOne()
                .getValue(SALES.ID);
    }

    @Override
    public void saveSaleItems(Long saleId, SaleTransaction sale) {
        sale.getItems().forEach(item ->
                dsl.insertInto(SALE_ITEMS)
                        .set(SALE_ITEMS.SALE_ID, saleId)
                        .set(SALE_ITEMS.PRODUCT_ID, item.getProductId())
                        .set(SALE_ITEMS.PRODUCT_NAME,
                                item.getProductName())
                        .set(SALE_ITEMS.QUANTITY, item.getQuantity())
                        .set(SALE_ITEMS.UNIT_PRICE,
                                toBigDecimal(item.getUnitPrice()))
                        .set(SALE_ITEMS.TAX_AMOUNT,
                                toBigDecimal(item.getTaxAmount()))
                        .set(SALE_ITEMS.DISCOUNT_AMOUNT,
                                toBigDecimal(item.getDiscountAmount()))
                        .set(SALE_ITEMS.LINE_TOTAL,
                                toBigDecimal(item.getLineTotal()))
                        .set(SALE_ITEMS.CURRENCY, "USD")
                        .execute()
        );
    }

    @Override
    public void savePayment(Payment payment) {
        dsl.insertInto(PAYMENTS)
                .set(PAYMENTS.SALE_ID, payment.getSaleId())
                .set(PAYMENTS.PAYMENT_TYPE,
                        payment.getPaymentType().name())
                .set(PAYMENTS.AMOUNT,
                        toBigDecimal(payment.getAmount()))
                .set(PAYMENTS.CURRENCY, "USD")
                .set(PAYMENTS.REFERENCE, payment.getReference())
                .execute();
    }

    @Override
    public void updateSaleStatus(Long saleId, String status,
                                 BigDecimal total) {
        dsl.update(SALES)
                .set(SALES.STATUS, status)
                .set(SALES.TOTAL_AMOUNT, total)
                .set(SALES.COMPLETED_AT,
                        java.time.LocalDateTime.now())
                .where(SALES.ID.eq(saleId))
                .execute();
    }

    @Override
    public Optional<SaleTransaction> findBySaleNumber(
            String saleNumber) {
        var record = dsl.selectFrom(SALES)
                .where(SALES.SALE_NUMBER.eq(saleNumber))
                .fetchOne();
        if (record == null) return Optional.empty();

        return Optional.of(new SaleTransaction(
                record.getId(),
                record.getSaleNumber(),
                record.getCashierId(),
                SaleStatus.valueOf(record.getStatus()),
                List.of(), // items loaded separately
                Money.of(record.getTaxAmount(), "USD"),
                Money.of(record.getDiscountAmount(), "USD"),
                record.getCreatedAt(),
                record.getCompletedAt()
        ));
    }

    @Override
    public List<SaleTransaction> findByDateRange(
            LocalDate from, LocalDate to) {
        return dsl.selectFrom(SALES)
                .where(SALES.CREATED_AT
                        .between(from.atStartOfDay(),
                                to.atTime(23, 59, 59)))
                .fetch()
                .map(r -> new SaleTransaction(
                        r.getId(), r.getSaleNumber(),
                        r.getCashierId(),
                        SaleStatus.valueOf(r.getStatus()),
                        List.of(),
                        Money.of(r.getTaxAmount(), "USD"),
                        Money.of(r.getDiscountAmount(), "USD"),
                        r.getCreatedAt(), r.getCompletedAt()
                ));
    }

    private BigDecimal toBigDecimal(
            javax.money.MonetaryAmount amount) {
        return amount.getNumber()
                .numberValue(BigDecimal.class);
    }
}