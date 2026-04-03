package com.turboboostteam.pos.dao;

import com.turboboostteam.pos.dao.impl.InventoryDaoImpl;
import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.model.inventory.StockMovement;
import com.turboboostteam.pos.model.inventory.StockMovement.MovementType;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class InventoryDaoTest extends BaseDbTest {

    static InventoryDao inventoryDao;

    @BeforeAll
    static void setup() {
        initDb();
        inventoryDao = new InventoryDaoImpl(dsl);
    }

    @Test
    @DisplayName("Should load all stock levels")
    void findAllStock_returnsSeedData() {
        List<StockLevel> stock = inventoryDao.findAllStock();
        assertThat(stock).isNotEmpty();
        assertThat(stock.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should find stock by product ID")
    void findByProductId_returnsCorrectStock() {
        // Coffee = product 1, seeded with quantity 50
        Optional<StockLevel> stock =
                inventoryDao.findByProductId(1L);
        assertThat(stock).isPresent();
        assertThat(stock.get().getQuantity()).isEqualTo(50);
        assertThat(stock.get().getLowStockThreshold()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should update stock quantity")
    void updateStock_changesQuantity() {
        // Update Coffee stock to 5
        inventoryDao.updateStock(1L, 5);

        Optional<StockLevel> stock =
                inventoryDao.findByProductId(1L);
        assertThat(stock).isPresent();
        assertThat(stock.get().getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Low stock alert fires correctly")
    void findLowStock_returnsItemsBelowThreshold() {
        // Set Coffee to 3 (threshold is 10 → low stock)
        inventoryDao.updateStock(1L, 3);

        List<StockLevel> lowStock = inventoryDao.findLowStock();
        assertThat(lowStock).isNotEmpty();
        assertThat(lowStock)
                .anyMatch(s -> s.getProductId().equals(1L));
    }

    @Test
    @DisplayName("StockLevel isLowStock() returns true when below threshold")
    void stockLevel_isLowStock_logic() {
        StockLevel stock = new StockLevel(
                1L, 1L, "Coffee", 3, 10);
        assertThat(stock.isLowStock()).isTrue();
        assertThat(stock.isOutOfStock()).isFalse();
    }

    @Test
    @DisplayName("StockLevel isOutOfStock() returns true when zero")
    void stockLevel_isOutOfStock_logic() {
        StockLevel stock = new StockLevel(
                1L, 1L, "Coffee", 0, 10);
        assertThat(stock.isOutOfStock()).isTrue();
        assertThat(stock.isLowStock()).isTrue();
    }

    @Test
    @DisplayName("Should record stock movement")
    void recordMovement_savedCorrectly() {
        StockMovement movement = new StockMovement(
                null, 1L,
                MovementType.ADJUSTMENT,
                -5,
                "Test adjustment",
                "admin",
                LocalDateTime.now()
        );

        inventoryDao.recordMovement(movement);

        List<StockMovement> movements =
                inventoryDao.findMovementsByProduct(1L);
        assertThat(movements).isNotEmpty();
        assertThat(movements.get(0).getMovementType())
                .isEqualTo(MovementType.ADJUSTMENT);
        assertThat(movements.get(0).getQuantityChange())
                .isEqualTo(-5);
    }
}