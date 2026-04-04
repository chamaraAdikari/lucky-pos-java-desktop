package com.turboboostteam.pos.model.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DailySalesReport {

    private final LocalDate date;
    private final int totalTransactions;
    private final BigDecimal totalRevenue;
    private final BigDecimal totalTax;
    private final BigDecimal totalDiscount;
    private final BigDecimal netRevenue;
    private final List<SaleReportItem> topProducts;
    private final List<HourlySale> hourlySales;
    private final String cashierName;

    public DailySalesReport(LocalDate date,
                            int totalTransactions,
                            BigDecimal totalRevenue,
                            BigDecimal totalTax,
                            BigDecimal totalDiscount,
                            BigDecimal netRevenue,
                            List<SaleReportItem> topProducts,
                            List<HourlySale> hourlySales,
                            String cashierName) {
        this.date              = date;
        this.totalTransactions = totalTransactions;
        this.totalRevenue      = totalRevenue;
        this.totalTax          = totalTax;
        this.totalDiscount     = totalDiscount;
        this.netRevenue        = netRevenue;
        this.topProducts       = topProducts;
        this.hourlySales       = hourlySales;
        this.cashierName       = cashierName;
    }

    public LocalDate getDate()               { return date; }
    public int getTotalTransactions()        { return totalTransactions; }
    public BigDecimal getTotalRevenue()      { return totalRevenue; }
    public BigDecimal getTotalTax()          { return totalTax; }
    public BigDecimal getTotalDiscount()     { return totalDiscount; }
    public BigDecimal getNetRevenue()        { return netRevenue; }
    public List<SaleReportItem> getTopProducts() { return topProducts; }
    public List<HourlySale> getHourlySales() { return hourlySales; }
    public String getCashierName()           { return cashierName; }
}