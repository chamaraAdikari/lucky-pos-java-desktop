package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.InventoryDao;
import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.model.inventory.StockMovement;
import com.turboboostteam.pos.model.inventory.StockMovement.MovementType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final InventoryDao inventoryDao;

    public InventoryService(InventoryDao inventoryDao) {
        this.inventoryDao = inventoryDao;
    }

    public List<StockLevel> getAllStock() {
        return inventoryDao.findAllStock();
    }

    public List<StockLevel> getLowStockItems() {
        return inventoryDao.findLowStock();
    }

    public Optional<StockLevel> getStockForProduct(Long productId) {
        return inventoryDao.findByProductId(productId);
    }

    // Called when a sale is made
    public void decrementStock(Long productId, int quantity, String username) {
        Optional<StockLevel> stock = inventoryDao.findByProductId(productId);
        stock.ifPresent(s -> {
            int newQty = Math.max(0, s.getQuantity() - quantity);
            inventoryDao.updateStock(productId, newQty);

            // Record movement
            inventoryDao.recordMovement(new StockMovement(
                    null, productId, MovementType.SALE,
                    -quantity,
                    "Sale deduction",
                    username,
                    LocalDateTime.now()
            ));

            // Audit log
            AuditLogger.log("STOCK_DECREMENTED", username,
                    "productId=" + productId + " qty=" + quantity);

            // Alert if low stock
            if (newQty <= s.getLowStockThreshold()) {
                AuditLogger.log("LOW_STOCK_ALERT", "SYSTEM",
                        "productId=" + productId
                                + " remaining=" + newQty);
            }
        });
    }

    // Manual stock adjustment
    public void adjustStock(Long productId, int newQuantity,
                            String notes, String username) {
        Optional<StockLevel> stock = inventoryDao.findByProductId(productId);
        stock.ifPresent(s -> {
            int change = newQuantity - s.getQuantity();
            inventoryDao.updateStock(productId, newQuantity);

            inventoryDao.recordMovement(new StockMovement(
                    null, productId,
                    change > 0 ? MovementType.RESTOCK : MovementType.ADJUSTMENT,
                    change, notes, username, LocalDateTime.now()
            ));

            AuditLogger.log("STOCK_ADJUSTED", username,
                    "productId=" + productId + " newQty=" + newQuantity);
        });
    }
}