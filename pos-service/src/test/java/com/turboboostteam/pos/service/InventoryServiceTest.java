package com.turboboostteam.pos.service;

import com.turboboostteam.pos.dao.InventoryDao;
import com.turboboostteam.pos.model.inventory.StockLevel;
import com.turboboostteam.pos.model.inventory.StockMovement;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    InventoryDao inventoryDao;

    InventoryService inventoryService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        inventoryService = new InventoryService(inventoryDao);

        // Setup fake session for AuditLogger
        SessionManager.getInstance().login(
                new com.turboboostteam.pos.model.user.User(
                        1L, "admin",
                        "hash",
                        com.turboboostteam.pos.model.user.Role.ADMIN,
                        true
                )
        );
    }

    @Test
    @DisplayName("Decrement stock reduces quantity correctly")
    void decrementStock_reducesQuantity() {
        // Arrange
        StockLevel stock = new StockLevel(1L, 1L, "Coffee", 50, 10);
        when(inventoryDao.findByProductId(1L))
                .thenReturn(Optional.of(stock));

        // Act
        inventoryService.decrementStock(1L, 5, "admin");

        // Assert — new qty should be 45
        verify(inventoryDao).updateStock(1L, 45);
        verify(inventoryDao).recordMovement(
                argThat(m -> m.getQuantityChange() == -5
                        && m.getMovementType()
                        == StockMovement.MovementType.SALE));
    }

    @Test
    @DisplayName("Decrement never goes below zero")
    void decrementStock_neverBelowZero() {
        StockLevel stock = new StockLevel(1L, 1L, "Coffee", 3, 10);
        when(inventoryDao.findByProductId(1L))
                .thenReturn(Optional.of(stock));

        inventoryService.decrementStock(1L, 10, "admin");

        // Should clamp to 0 not go negative
        verify(inventoryDao).updateStock(1L, 0);
    }

    @Test
    @DisplayName("Adjust stock records RESTOCK when increasing")
    void adjustStock_recordsRestockMovement() {
        StockLevel stock = new StockLevel(1L, 1L, "Coffee", 10, 5);
        when(inventoryDao.findByProductId(1L))
                .thenReturn(Optional.of(stock));

        inventoryService.adjustStock(1L, 50, "Restock delivery", "admin");

        verify(inventoryDao).updateStock(1L, 50);
        verify(inventoryDao).recordMovement(
                argThat(m -> m.getMovementType()
                        == StockMovement.MovementType.RESTOCK
                        && m.getQuantityChange() == 40));
    }

    @Test
    @DisplayName("Adjust stock records ADJUSTMENT when decreasing")
    void adjustStock_recordsAdjustmentMovement() {
        StockLevel stock = new StockLevel(1L, 1L, "Coffee", 50, 5);
        when(inventoryDao.findByProductId(1L))
                .thenReturn(Optional.of(stock));

        inventoryService.adjustStock(1L, 30, "Damage", "admin");

        verify(inventoryDao).updateStock(1L, 30);
        verify(inventoryDao).recordMovement(
                argThat(m -> m.getMovementType()
                        == StockMovement.MovementType.ADJUSTMENT
                        && m.getQuantityChange() == -20));
    }

    @Test
    @DisplayName("Get low stock items delegates to DAO")
    void getLowStockItems_delegatesToDao() {
        inventoryService.getLowStockItems();
        verify(inventoryDao).findLowStock();
    }
}