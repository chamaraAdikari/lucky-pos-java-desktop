package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.DiscountDao;
import com.turboboostteam.pos.model.discount.Discount;
import com.turboboostteam.pos.model.discount.DiscountType;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.DISCOUNTS;

public class DiscountDaoImpl implements DiscountDao {

    private final DSLContext dsl;

    public DiscountDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Discount> findAll() {
        return dsl.selectFrom(DISCOUNTS)
                .where(DISCOUNTS.ACTIVE.isTrue())
                .fetch()
                .map(this::toDiscount);
    }

    @Override
    public Optional<Discount> findByCode(String code) {
        Record record = dsl.selectFrom(DISCOUNTS)
                .where(DISCOUNTS.CODE.eq(
                        code.toUpperCase().trim()))
                .fetchOne();
        return Optional.ofNullable(record)
                .map(this::toDiscount);
    }

    @Override
    public void save(Discount discount) {
        dsl.insertInto(DISCOUNTS)
                .set(DISCOUNTS.CODE, discount.getCode())
                .set(DISCOUNTS.NAME, discount.getName())
                .set(DISCOUNTS.DISCOUNT_TYPE,
                        discount.getDiscountType().name())
                .set(DISCOUNTS.DISCOUNT_VALUE,
                        discount.getDiscountValue())
                .set(DISCOUNTS.MIN_PURCHASE,
                        discount.getMinPurchase())
                .set(DISCOUNTS.ACTIVE, discount.isActive())
                .execute();
    }

    @Override
    public void deactivate(Long id) {
        dsl.update(DISCOUNTS)
                .set(DISCOUNTS.ACTIVE, false)
                .where(DISCOUNTS.ID.eq(id))
                .execute();
    }

    private Discount toDiscount(Record r) {
        return new Discount(
                r.get(DISCOUNTS.ID),
                r.get(DISCOUNTS.CODE),
                r.get(DISCOUNTS.NAME),
                DiscountType.valueOf(
                        r.get(DISCOUNTS.DISCOUNT_TYPE)),
                r.get(DISCOUNTS.DISCOUNT_VALUE),
                r.get(DISCOUNTS.MIN_PURCHASE),
                r.get(DISCOUNTS.ACTIVE),
                r.get(DISCOUNTS.VALID_FROM),
                r.get(DISCOUNTS.VALID_UNTIL)
        );
    }
}