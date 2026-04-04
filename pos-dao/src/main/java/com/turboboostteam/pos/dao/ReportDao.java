package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.report.*;
import java.time.LocalDate;
import java.util.List;

public interface ReportDao {
    DailySalesReport getDailyReport(LocalDate date,
                                    String cashierName);
    List<SaleReportItem> getTopProducts(LocalDate from,
                                        LocalDate to,
                                        int limit);
    List<HourlySale> getHourlySales(LocalDate date);
}