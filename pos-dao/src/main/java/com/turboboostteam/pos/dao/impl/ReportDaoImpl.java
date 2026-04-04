package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.ReportDao;
import com.turboboostteam.pos.model.report.*;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.turboboostteam.pos.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public class ReportDaoImpl implements ReportDao {

    private final DSLContext dsl;

    public ReportDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public DailySalesReport getDailyReport(
            LocalDate date, String cashierName) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.atTime(23, 59, 59);

        // Aggregate sales for the day
        Record summary = dsl.select(
                        count(SALES.ID).as("total_transactions"),
                        sum(SALES.TOTAL_AMOUNT).as("total_revenue"),
                        sum(SALES.TAX_AMOUNT).as("total_tax"),
                        sum(SALES.DISCOUNT_AMOUNT)
                                .as("total_discount"))
                .from(SALES)
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT
                                .between(start, end)))
                .fetchOne();

        int totalTransactions = summary != null
                ? summary.get("total_transactions",
                Integer.class) : 0;

        BigDecimal totalRevenue = summary != null
                && summary.get("total_revenue",
                BigDecimal.class) != null
                ? summary.get("total_revenue",
                BigDecimal.class)
                : BigDecimal.ZERO;

        BigDecimal totalTax = summary != null
                && summary.get("total_tax",
                BigDecimal.class) != null
                ? summary.get("total_tax",
                BigDecimal.class)
                : BigDecimal.ZERO;

        BigDecimal totalDiscount = summary != null
                && summary.get("total_discount",
                BigDecimal.class) != null
                ? summary.get("total_discount",
                BigDecimal.class)
                : BigDecimal.ZERO;

        BigDecimal netRevenue = totalRevenue
                .subtract(totalTax);

        List<SaleReportItem> topProducts =
                getTopProducts(date, date, 10);
        List<HourlySale> hourlySales =
                getHourlySales(date);

        return new DailySalesReport(
                date, totalTransactions,
                totalRevenue, totalTax,
                totalDiscount, netRevenue,
                topProducts, hourlySales,
                cashierName);
    }

    @Override
    public List<SaleReportItem> getTopProducts(
            LocalDate from, LocalDate to, int limit) {

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(23, 59, 59);

        return dsl.select(
                        SALE_ITEMS.PRODUCT_NAME,
                        sum(SALE_ITEMS.QUANTITY).as("qty_sold"),
                        sum(SALE_ITEMS.LINE_TOTAL).as("revenue"))
                .from(SALE_ITEMS)
                .join(SALES)
                .on(SALE_ITEMS.SALE_ID.eq(SALES.ID))
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT
                                .between(start, end)))
                .groupBy(SALE_ITEMS.PRODUCT_NAME)
                .orderBy(
                        sum(SALE_ITEMS.LINE_TOTAL).desc())
                .limit(limit)
                .fetch()
                .map(r -> new SaleReportItem(
                        r.get(SALE_ITEMS.PRODUCT_NAME),
                        r.get("qty_sold",
                                Integer.class),
                        r.get("revenue",
                                BigDecimal.class)));
    }

    @Override
    public List<HourlySale> getHourlySales(
            LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end   = date.atTime(23, 59, 59);

        return dsl.select(
                        hour(SALES.CREATED_AT).as("hour"),
                        count(SALES.ID).as("transactions"),
                        sum(SALES.TOTAL_AMOUNT).as("revenue"))
                .from(SALES)
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT
                                .between(start, end)))
                .groupBy(hour(SALES.CREATED_AT))
                .orderBy(hour(SALES.CREATED_AT).asc())
                .fetch()
                .map(r -> new HourlySale(
                        r.get("hour", Integer.class),
                        r.get("transactions",
                                Integer.class),
                        r.get("revenue",
                                BigDecimal.class) != null
                                ? r.get("revenue",
                                BigDecimal.class)
                                : BigDecimal.ZERO));
    }
}