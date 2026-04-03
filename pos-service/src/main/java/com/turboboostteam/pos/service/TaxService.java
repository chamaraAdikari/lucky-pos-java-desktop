package com.turboboostteam.pos.service;

import com.turboboostteam.pos.model.sale.SaleItem;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class TaxService {

    // Strategy Pattern — tax rates by category
    private static final Map<String, Double> TAX_RATES
            = new HashMap<>();

    static {
        TAX_RATES.put("Food & Beverage", 0.05); // 5%
        TAX_RATES.put("Electronics",     0.15); // 15%
        TAX_RATES.put("Clothing",        0.10); // 10%
        TAX_RATES.put("General",         0.08); // 8%
        TAX_RATES.put("BUNDLE",          0.00); // 0% bundles
    }

    public MonetaryAmount calculate(SaleItem item,
                                    String categoryName) {
        double rate = getTaxRate(categoryName);

        double unitPrice = item.getUnitPrice()
                .getNumber()
                .doubleValue();

        double taxAmount = unitPrice
                * item.getQuantity()
                * rate;

        BigDecimal rounded = BigDecimal
                .valueOf(taxAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return Money.of(rounded, "USD");
    }

    public double getTaxRate(String categoryName) {
        return TAX_RATES.getOrDefault(categoryName, 0.08);
    }

    // Easy to add new categories later
    public void addTaxRate(String category, double rate) {
        TAX_RATES.put(category, rate);
    }
}