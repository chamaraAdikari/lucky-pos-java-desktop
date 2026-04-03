-- Inventory stock table
CREATE TABLE IF NOT EXISTS inventory_stock (
                                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               product_id      BIGINT NOT NULL REFERENCES products(id),
    quantity        INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 5,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Stock movements audit table
CREATE TABLE IF NOT EXISTS stock_movements (
                                               id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               product_id      BIGINT NOT NULL REFERENCES products(id),
    movement_type   VARCHAR(20) NOT NULL, -- SALE, ADJUSTMENT, RESTOCK
    quantity_change INT NOT NULL,          -- negative = decrease
    notes           VARCHAR(255),
    created_by      VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Seed initial stock for all products
INSERT INTO inventory_stock (product_id, quantity, low_stock_threshold)
VALUES
    (1, 50, 10),   -- Coffee
    (2, 30, 5),    -- Sandwich
    (3, 20, 3),    -- USB Cable
    (4, 15, 5),    -- T-Shirt
    (5, 25, 5);    -- Lunch Bundle