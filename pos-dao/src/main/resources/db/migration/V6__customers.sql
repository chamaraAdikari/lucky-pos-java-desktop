-- Customers table
CREATE TABLE IF NOT EXISTS customers (
                                         id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) UNIQUE,
    phone           VARCHAR(20)  UNIQUE,
    loyalty_points  INT NOT NULL DEFAULT 0,
    loyalty_tier    VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    total_spent     DECIMAL(10,2) NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- Seed sample customers
INSERT INTO customers
(first_name, last_name, email, phone,
 loyalty_points, loyalty_tier, total_spent)
VALUES
    ('John',  'Silva',   'john@email.com',  '+94771234567',
     150, 'BRONZE', 75.50),
    ('Mary',  'Perera',  'mary@email.com',  '+94779876543',
     850, 'SILVER', 425.00),
    ('David', 'Fernando','david@email.com', '+94712345678',
     2100, 'GOLD',  1050.00);