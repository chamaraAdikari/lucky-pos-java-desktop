-- Discount/promotions table
CREATE TABLE IF NOT EXISTS discounts (
                                         id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         code            VARCHAR(50) UNIQUE,
    name            VARCHAR(100) NOT NULL,
    discount_type   VARCHAR(20) NOT NULL, -- PERCENTAGE, FLAT, BUY_X_GET_Y
    discount_value  DECIMAL(10,2) NOT NULL,
    min_purchase    DECIMAL(10,2) NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from      TIMESTAMP,
    valid_until     TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Seed sample discounts
INSERT INTO discounts
(code, name, discount_type, discount_value, min_purchase)
VALUES
    ('SAVE10',   '10% Off Everything',    'PERCENTAGE', 10.00, 0.00),
    ('SAVE5',    'USD 5 Flat Discount',   'FLAT',        5.00, 20.00),
    ('WELCOME',  'Welcome 15% Off',       'PERCENTAGE', 15.00, 0.00),
    ('BULK20',   '20% Off Over USD 50',   'PERCENTAGE', 20.00, 50.00);