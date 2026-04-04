package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.sale.Payment;
import com.turboboostteam.pos.model.sale.SaleItem;
import com.turboboostteam.pos.model.sale.SaleTransaction;

import java.util.List;
import java.util.Optional;

public interface SaleDao {
    Long saveSale(SaleTransaction sale);
    void saveSaleItems(Long saleId, SaleTransaction sale);
    void savePayment(Payment payment);
    void updateSaleStatus(Long saleId, String status,
                          java.math.BigDecimal total);
    void voidSale(Long saleId);
    void refundSale(Long saleId, String status);

    List<SaleTransaction> findRecentSales(int limit);
    Optional<SaleTransaction> findById(Long id);
    List<SaleItem> findItemsBySaleId(Long saleId);
    Optional<SaleTransaction> findBySaleNumber(String saleNumber);
    List<SaleTransaction> findByDateRange(
            java.time.LocalDate from,
            java.time.LocalDate to);
}