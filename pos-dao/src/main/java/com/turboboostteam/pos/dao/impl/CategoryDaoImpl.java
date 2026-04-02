package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.CategoryDao;
import com.turboboostteam.pos.model.product.Category;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.CATEGORIES;

public class CategoryDaoImpl implements CategoryDao {

    private final DSLContext dsl;

    public CategoryDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<Category> findAll() {
        return dsl.selectFrom(CATEGORIES)
                .fetch()
                .map(this::toCategory);
    }

    @Override
    public Optional<Category> findById(Long id) {
        Record record = dsl.selectFrom(CATEGORIES)
                .where(CATEGORIES.ID.eq(id))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toCategory);
    }

    @Override
    public void save(Category category) {
        dsl.insertInto(CATEGORIES)
                .set(CATEGORIES.NAME, category.getName())
                .set(CATEGORIES.DESCRIPTION, category.getDescription())
                .execute();
    }

    private Category toCategory(Record r) {
        return new Category(
                r.get(CATEGORIES.ID),
                r.get(CATEGORIES.NAME),
                r.get(CATEGORIES.DESCRIPTION)
        );
    }
}