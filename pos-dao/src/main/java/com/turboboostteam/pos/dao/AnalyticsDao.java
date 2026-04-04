package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.report.SaleReportItem;
import com.turboboostteam.pos.model.report.HourlySale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnalyticsDao {
    // Daily revenue for last N days
    Map<LocalDate, BigDecimal> getDailyRevenue(
            LocalDate from, LocalDate to);

    // Top N products by revenue
    List<SaleReportItem> getTopProducts(
            LocalDate from, LocalDate to, int limit);

    // Hourly sales for a specific day
    List<HourlySale> getHourlySales(LocalDate date);

    // Revenue by category
    Map<String, BigDecimal> getRevenueByCategory(
            LocalDate from, LocalDate to);
}