-- Sales table
CREATE TABLE IF NOT EXISTS sales (
                                     id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     sale_number     VARCHAR(50) NOT NULL UNIQUE,
    cashier_id      BIGINT NOT NULL REFERENCES users(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'USD',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP
    );

-- Sale items table
CREATE TABLE IF NOT EXISTS sale_items (
                                          id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          sale_id         BIGINT NOT NULL REFERENCES sales(id),
    product_id      BIGINT NOT NULL REFERENCES products(id),
    product_name    VARCHAR(150) NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10,2) NOT NULL,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    line_total      DECIMAL(10,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD'
    );

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
                                        id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        sale_id         BIGINT NOT NULL REFERENCES sales(id),
    payment_type    VARCHAR(20) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'USD',
    reference       VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );