package com.turboboostteam.pos.model.report;

import java.math.BigDecimal;

public class HourlySale {
    private final int hour;
    private final int transactions;
    private final BigDecimal revenue;

    public HourlySale(int hour,
                      int transactions,
                      BigDecimal revenue) {
        this.hour         = hour;
        this.transactions = transactions;
        this.revenue      = revenue;
    }

    public int getHour()               { return hour; }
    public int getTransactions()       { return transactions; }
    public BigDecimal getRevenue()     { return revenue; }

    public String getHourLabel() {
        return String.format("%02d:00", hour);
    }
}