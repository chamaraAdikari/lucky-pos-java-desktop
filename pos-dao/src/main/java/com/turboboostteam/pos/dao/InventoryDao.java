package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.model.inventory.StockMovement;
import java.util.List;
import java.util.Optional;

public interface InventoryDao {
    List<StockLevel> findAllStock();
    Optional<StockLevel> findByProductId(Long productId);
    List<StockLevel> findLowStock();
    void updateStock(Long productId, int newQuantity);
    void recordMovement(StockMovement movement);
    List<StockMovement> findMovementsByProduct(Long productId);
}