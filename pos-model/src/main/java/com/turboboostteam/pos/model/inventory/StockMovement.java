package com.turboboostteam.pos.model.inventory;

import java.time.LocalDateTime;

public class StockMovement {

    public enum MovementType {
        SALE, ADJUSTMENT, RESTOCK
    }

    private Long id;
    private Long productId;
    private MovementType movementType;
    private int quantityChange;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;

    public StockMovement(Long id, Long productId,
                         MovementType movementType,
                         int quantityChange, String notes,
                         String createdBy, LocalDateTime createdAt) {
        this.id             = id;
        this.productId      = productId;
        this.movementType   = movementType;
        this.quantityChange = quantityChange;
        this.notes          = notes;
        this.createdBy      = createdBy;
        this.createdAt      = createdAt;
    }

    public Long getId()                    { return id; }
    public Long getProductId()             { return productId; }
    public MovementType getMovementType()  { return movementType; }
    public int getQuantityChange()         { return quantityChange; }
    public String getNotes()               { return notes; }
    public String getCreatedBy()           { return createdBy; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
}