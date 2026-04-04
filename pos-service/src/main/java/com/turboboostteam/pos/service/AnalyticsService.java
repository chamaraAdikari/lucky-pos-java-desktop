package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.AnalyticsDao;
import com.turboboostteam.pos.model.report.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AnalyticsService {

    private final AnalyticsDao analyticsDao;

    public AnalyticsService(AnalyticsDao analyticsDao) {
        this.analyticsDao = analyticsDao;
    }

    public Map<LocalDate, BigDecimal> getLast7DaysRevenue() {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(6);
        return analyticsDao.getDailyRevenue(from, to);
    }

    public Map<LocalDate, BigDecimal> getLast30DaysRevenue() {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(29);
        return analyticsDao.getDailyRevenue(from, to);
    }

    public Map<LocalDate, BigDecimal> getDateRangeRevenue(
            LocalDate from, LocalDate to) {
        return analyticsDao.getDailyRevenue(from, to);
    }

    public List<SaleReportItem> getTopProducts(
            LocalDate from, LocalDate to, int limit) {
        return analyticsDao.getTopProducts(
                from, to, limit);
    }

    public List<HourlySale> getTodayHourly() {
        return analyticsDao.getHourlySales(
                LocalDate.now());
    }

    public Map<String, BigDecimal> getCategoryRevenue(
            LocalDate from, LocalDate to) {
        return analyticsDao.getRevenueByCategory(
                from, to);
    }
}