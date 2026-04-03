package com.turboboostteam.pos.dao.impl;

import com.turboboostteam.pos.dao.InventoryDao;
import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.model.inventory.StockMovement;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.turboboostteam.pos.jooq.Tables.*;

public class InventoryDaoImpl implements InventoryDao {

    private final DSLContext dsl;

    public InventoryDaoImpl(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<StockLevel> findAllStock() {
        return dsl.select()
                .from(INVENTORY_STOCK)
                .leftJoin(PRODUCTS)
                .on(INVENTORY_STOCK.PRODUCT_ID.eq(PRODUCTS.ID))
                .fetch()
                .map(this::toStockLevel);
    }

    @Override
    public Optional<StockLevel> findByProductId(Long productId) {
        Record record = dsl.select()
                .from(INVENTORY_STOCK)
                .leftJoin(PRODUCTS)
                .on(INVENTORY_STOCK.PRODUCT_ID.eq(PRODUCTS.ID))
                .where(INVENTORY_STOCK.PRODUCT_ID.eq(productId))
                .fetchOne();
        return Optional.ofNullable(record).map(this::toStockLevel);
    }

    @Override
    public List<StockLevel> findLowStock() {
        return dsl.select()
                .from(INVENTORY_STOCK)
                .leftJoin(PRODUCTS)
                .on(INVENTORY_STOCK.PRODUCT_ID.eq(PRODUCTS.ID))
                .where(INVENTORY_STOCK.QUANTITY
                        .le(INVENTORY_STOCK.LOW_STOCK_THRESHOLD))
                .fetch()
                .map(this::toStockLevel);
    }

    @Override
    public void updateStock(Long productId, int newQuantity) {
        dsl.update(INVENTORY_STOCK)
                .set(INVENTORY_STOCK.QUANTITY, newQuantity)
                .set(INVENTORY_STOCK.UPDATED_AT, LocalDateTime.now())
                .where(INVENTORY_STOCK.PRODUCT_ID.eq(productId))
                .execute();
    }

    @Override
    public void recordMovement(StockMovement movement) {
        dsl.insertInto(STOCK_MOVEMENTS)
                .set(STOCK_MOVEMENTS.PRODUCT_ID,
                        movement.getProductId())
                .set(STOCK_MOVEMENTS.MOVEMENT_TYPE,
                        movement.getMovementType().name())
                .set(STOCK_MOVEMENTS.QUANTITY_CHANGE,
                        movement.getQuantityChange())
                .set(STOCK_MOVEMENTS.NOTES, movement.getNotes())
                .set(STOCK_MOVEMENTS.CREATED_BY, movement.getCreatedBy())
                .execute();
    }

    @Override
    public List<StockMovement> findMovementsByProduct(Long productId) {
        return dsl.selectFrom(STOCK_MOVEMENTS)
                .where(STOCK_MOVEMENTS.PRODUCT_ID.eq(productId))
                .orderBy(STOCK_MOVEMENTS.CREATED_AT.desc())
                .fetch()
                .map(r -> new StockMovement(
                        r.get(STOCK_MOVEMENTS.ID),
                        r.get(STOCK_MOVEMENTS.PRODUCT_ID),
                        StockMovement.MovementType.valueOf(
                                r.get(STOCK_MOVEMENTS.MOVEMENT_TYPE)),
                        r.get(STOCK_MOVEMENTS.QUANTITY_CHANGE),
                        r.get(STOCK_MOVEMENTS.NOTES),
                        r.get(STOCK_MOVEMENTS.CREATED_BY),
                        r.get(STOCK_MOVEMENTS.CREATED_AT)
                ));
    }

    private StockLevel toStockLevel(Record r) {
        return new StockLevel(
                r.get(INVENTORY_STOCK.ID),
                r.get(INVENTORY_STOCK.PRODUCT_ID),
                r.get(PRODUCTS.NAME),
                r.get(INVENTORY_STOCK.QUANTITY),
                r.get(INVENTORY_STOCK.LOW_STOCK_THRESHOLD)
        );
    }
}