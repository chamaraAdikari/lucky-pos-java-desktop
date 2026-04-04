package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.AnalyticsDao;
import com.turboboostteam.pos.model.report.*;
import org.jooq.DSLContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.turboboostteam.pos.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public class AnalyticsDaoImpl implements AnalyticsDao {

    private final DSLContext dsl;

    public AnalyticsDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public Map<LocalDate, BigDecimal> getDailyRevenue(
            LocalDate from, LocalDate to) {

        Map<LocalDate, BigDecimal> result =
                new LinkedHashMap<>();

        // Initialize all dates with zero
        LocalDate current = from;
        while (!current.isAfter(to)) {
            result.put(current, BigDecimal.ZERO);
            current = current.plusDays(1);
        }

        // Fill from DB
        dsl.select(
                        SALES.CREATED_AT.cast(
                                        org.jooq.impl.SQLDataType.DATE)
                                .as("sale_date"),
                        sum(SALES.TOTAL_AMOUNT).as("revenue"))
                .from(SALES)
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT.between(
                                from.atStartOfDay(),
                                to.atTime(23, 59, 59))))
                .groupBy(SALES.CREATED_AT.cast(
                        org.jooq.impl.SQLDataType.DATE))
                .fetch()
                .forEach(r -> {
                    LocalDate date = r.get(
                            "sale_date",
                            LocalDate.class);
                    BigDecimal revenue = r.get(
                            "revenue",
                            BigDecimal.class);
                    if (date != null
                            && revenue != null) {
                        result.put(date, revenue);
                    }
                });

        return result;
    }

    @Override
    public List<SaleReportItem> getTopProducts(
            LocalDate from, LocalDate to, int limit) {

        return dsl.select(
                        SALE_ITEMS.PRODUCT_NAME,
                        sum(SALE_ITEMS.QUANTITY).as("qty"),
                        sum(SALE_ITEMS.LINE_TOTAL)
                                .as("revenue"))
                .from(SALE_ITEMS)
                .join(SALES)
                .on(SALE_ITEMS.SALE_ID.eq(SALES.ID))
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT.between(
                                from.atStartOfDay(),
                                to.atTime(23, 59, 59))))
                .groupBy(SALE_ITEMS.PRODUCT_NAME)
                .orderBy(
                        sum(SALE_ITEMS.LINE_TOTAL).desc())
                .limit(limit)
                .fetch()
                .map(r -> new SaleReportItem(
                        r.get(SALE_ITEMS.PRODUCT_NAME),
                        r.get("qty", Integer.class) != null
                                ? r.get("qty",
                                Integer.class) : 0,
                        r.get("revenue",
                                BigDecimal.class) != null
                                ? r.get("revenue",
                                BigDecimal.class)
                                : BigDecimal.ZERO));
    }

    @Override
    public List<HourlySale> getHourlySales(
            LocalDate date) {

        return dsl.select(
                        hour(SALES.CREATED_AT).as("hour"),
                        count(SALES.ID).as("transactions"),
                        sum(SALES.TOTAL_AMOUNT).as("revenue"))
                .from(SALES)
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT.between(
                                date.atStartOfDay(),
                                date.atTime(23, 59, 59))))
                .groupBy(hour(SALES.CREATED_AT))
                .orderBy(hour(SALES.CREATED_AT))
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

    @Override
    public Map<String, BigDecimal> getRevenueByCategory(
            LocalDate from, LocalDate to) {

        Map<String, BigDecimal> result =
                new LinkedHashMap<>();

        dsl.select(
                        PRODUCTS.CATEGORY_ID,
                        CATEGORIES.NAME.as("cat_name"),
                        sum(SALE_ITEMS.LINE_TOTAL)
                                .as("revenue"))
                .from(SALE_ITEMS)
                .join(SALES)
                .on(SALE_ITEMS.SALE_ID.eq(SALES.ID))
                .join(PRODUCTS)
                .on(SALE_ITEMS.PRODUCT_ID
                        .eq(PRODUCTS.ID))
                .join(CATEGORIES)
                .on(PRODUCTS.CATEGORY_ID
                        .eq(CATEGORIES.ID))
                .where(SALES.STATUS.eq("COMMITTED")
                        .and(SALES.CREATED_AT.between(
                                from.atStartOfDay(),
                                to.atTime(23, 59, 59))))
                .groupBy(PRODUCTS.CATEGORY_ID,
                        CATEGORIES.NAME)
                .orderBy(
                        sum(SALE_ITEMS.LINE_TOTAL).desc())
                .fetch()
                .forEach(r -> {
                    String cat = r.get("cat_name",
                            String.class);
                    BigDecimal rev = r.get("revenue",
                            BigDecimal.class);
                    if (cat != null && rev != null) {
                        result.put(cat, rev);
                    }
                });

        return result;
    }
}