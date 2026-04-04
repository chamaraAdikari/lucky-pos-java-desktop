package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.DiscountDao;
import com.turboboostteam.pos.model.discount.Discount;
import com.turboboostteam.pos.model.sale.SaleTransaction;

import javax.money.MonetaryAmount;
import java.util.Optional;

public class DiscountService {

    private final DiscountDao discountDao;

    public DiscountService(DiscountDao discountDao) {
        this.discountDao = discountDao;
    }

    // Apply coupon code to sale
    public Optional<AppliedDiscount> applyCode(
            String code, SaleTransaction sale) {

        Optional<Discount> discountOpt =
                discountDao.findByCode(code);

        if (discountOpt.isEmpty()) {
            return Optional.empty();
        }

        Discount discount = discountOpt.get();
        MonetaryAmount cartTotal = sale.getSubtotal();

        if (!discount.isValid(cartTotal)) {
            return Optional.empty();
        }

        MonetaryAmount discountAmount =
                discount.calculate(cartTotal);

        AuditLogger.log("DISCOUNT_APPLIED",
                SessionManager.getInstance()
                        .getCurrentUser().getUsername(),
                "code=" + code
                        + " amount=" + discountAmount);

        return Optional.of(new AppliedDiscount(
                discount, discountAmount));
    }

    // Value object — result of applying discount
    public record AppliedDiscount(
            Discount discount,
            MonetaryAmount discountAmount) {

        public String getDescription() {
            return discount.getName()
                    + " → -" + discountAmount;
        }
    }
}