package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.sale.Payment;
import com.turboboostteam.pos.model.sale.SaleTransaction;

import java.util.List;
import java.util.Optional;

public interface SaleDao {
    Long saveSale(SaleTransaction sale);
    void saveSaleItems(Long saleId, SaleTransaction sale);
    void savePayment(Payment payment);
    void updateSaleStatus(Long saleId, String status,
                          java.math.BigDecimal total);
    Optional<SaleTransaction> findBySaleNumber(String saleNumber);
    List<SaleTransaction> findByDateRange(
            java.time.LocalDate from,
            java.time.LocalDate to);
}