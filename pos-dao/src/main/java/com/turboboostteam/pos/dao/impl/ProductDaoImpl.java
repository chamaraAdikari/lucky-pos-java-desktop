package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.ProductDao;
import com.turboboostteam.pos.model.product.*;
import org.javamoney.moneta.Money;
import org.jooq.DSLContext;
import org.jooq.Record;

import javax.money.MonetaryAmount;
import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.CATEGORIES;
import static com.turboboostteam.pos.jooq.Tables.PRODUCTS;

public class ProductDaoImpl implements ProductDao {

    private final DSLContext dsl;

    public ProductDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Product> findAll() {
        return dsl.select()
                .from(PRODUCTS)
                .leftJoin(CATEGORIES)
                .on(PRODUCTS.CATEGORY_ID.eq(CATEGORIES.ID))
                .where(PRODUCTS.ACTIVE.isTrue())
                .fetch()
                .map(this::toProduct);
    }

    @Override
    public Optional<Product> findById(Long id) {
        Record record = dsl.select()
                .from(PRODUCTS)
                .leftJoin(CATEGORIES)
                .on(PRODUCTS.CATEGORY_ID.eq(CATEGORIES.ID))
                .where(PRODUCTS.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toProduct);
    }

    @Override
    public Optional<Product> findByBarcode(String barcode) {
        Record record = dsl.select()
                .from(PRODUCTS)
                .leftJoin(CATEGORIES)
                .on(PRODUCTS.CATEGORY_ID.eq(CATEGORIES.ID))
                .where(PRODUCTS.BARCODE.eq(barcode))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toProduct);
    }

    @Override
    public List<Product> findByCategory(Long categoryId) {
        return dsl.select()
                .from(PRODUCTS)
                .leftJoin(CATEGORIES)
                .on(PRODUCTS.CATEGORY_ID.eq(CATEGORIES.ID))
                .where(PRODUCTS.CATEGORY_ID.eq(categoryId)
                        .and(PRODUCTS.ACTIVE.isTrue()))
                .fetch()
                .map(this::toProduct);
    }

    @Override
    public void save(Product product) {
        dsl.insertInto(PRODUCTS)
                .set(PRODUCTS.NAME, product.getName())
                .set(PRODUCTS.BARCODE, product.getBarcode())
                .set(PRODUCTS.PRICE,
                        product.getPrice().getNumber()
                                .numberValue(java.math.BigDecimal.class))
                .set(PRODUCTS.CURRENCY,
                        product.getPrice().getCurrency().getCurrencyCode())
                .set(PRODUCTS.PRODUCT_TYPE, product.getProductType())
                .set(PRODUCTS.CATEGORY_ID,
                        product.getCategory() != null
                                ? product.getCategory().getId() : null)
                .set(PRODUCTS.ACTIVE, product.isActive())
                .execute();
    }

    @Override
    public void update(Product product) {
        dsl.update(PRODUCTS)
                .set(PRODUCTS.NAME, product.getName())
                .set(PRODUCTS.BARCODE, product.getBarcode())
                .set(PRODUCTS.PRICE,
                        product.getPrice().getNumber()
                                .numberValue(java.math.BigDecimal.class))
                .set(PRODUCTS.CURRENCY,
                        product.getPrice().getCurrency().getCurrencyCode())
                .set(PRODUCTS.CATEGORY_ID,
                        product.getCategory() != null
                                ? product.getCategory().getId() : null)
                .where(PRODUCTS.ID.eq(product.getId()))
                .execute();
    }

    @Override
    public void deactivate(Long id) {
        dsl.update(PRODUCTS)
                .set(PRODUCTS.ACTIVE, false)
                .where(PRODUCTS.ID.eq(id))
                .execute();
    }

    // Map DB record → Product object (Polymorphism)
    private Product toProduct(Record r) {
        MonetaryAmount price = Money.of(
                r.get(PRODUCTS.PRICE),
                r.get(PRODUCTS.CURRENCY)
        );

        Category category = null;
        if (r.get(CATEGORIES.ID) != null) {
            category = new Category(
                    r.get(CATEGORIES.ID),
                    r.get(CATEGORIES.NAME),
                    r.get(CATEGORIES.DESCRIPTION)
            );
        }

        String type = r.get(PRODUCTS.PRODUCT_TYPE);

        if ("BUNDLE".equals(type)) {
            return new BundleProduct(
                    r.get(PRODUCTS.ID),
                    r.get(PRODUCTS.NAME),
                    r.get(PRODUCTS.BARCODE),
                    price, category,
                    r.get(PRODUCTS.ACTIVE),
                    List.of(), 0.0   // items loaded separately
            );
        }

        return new SimpleProduct(
                r.get(PRODUCTS.ID),
                r.get(PRODUCTS.NAME),
                r.get(PRODUCTS.BARCODE),
                price, category,
                r.get(PRODUCTS.ACTIVE)
        );
    }
}