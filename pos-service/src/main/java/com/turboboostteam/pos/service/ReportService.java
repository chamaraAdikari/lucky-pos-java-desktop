package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.ReportDao;
import com.turboboostteam.pos.model.report.*;

import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDao reportDao;

    public ReportService(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public DailySalesReport getDailyReport(
            LocalDate date) {
        String cashier = SessionManager.getInstance()
                .getCurrentUser().getUsername();
        return reportDao.getDailyReport(date, cashier);
    }

    public DailySalesReport getTodayReport() {
        return getDailyReport(LocalDate.now());
    }

    public List<SaleReportItem> getTopProducts(
            LocalDate from, LocalDate to, int limit) {
        return reportDao.getTopProducts(from, to, limit);
    }

    public List<HourlySale> getHourlySales(
            LocalDate date) {
        return reportDao.getHourlySales(date);
    }
}