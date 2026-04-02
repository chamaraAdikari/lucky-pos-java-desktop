-- Categories table
CREATE TABLE categories (
                            id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name        VARCHAR(100) NOT NULL UNIQUE,
                            description VARCHAR(255),
                            created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
                          id           BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name         VARCHAR(150) NOT NULL,
                          barcode      VARCHAR(50)  UNIQUE,
                          price        DECIMAL(10,2) NOT NULL,
                          currency     VARCHAR(3)   NOT NULL DEFAULT 'USD',
                          product_type VARCHAR(20)  NOT NULL DEFAULT 'SIMPLE',
                          category_id  BIGINT REFERENCES categories(id),
                          active       BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default categories
INSERT INTO categories (name, description) VALUES
                                               ('Food & Beverage', 'Edible products and drinks'),
                                               ('Electronics',     'Electronic devices and accessories'),
                                               ('Clothing',        'Apparel and fashion items'),
                                               ('General',         'Miscellaneous products');

-- Seed sample products
INSERT INTO products (name, barcode, price, currency, product_type, category_id) VALUES
                                                                                     ('Coffee',        '4901234567890', 2.50,  'USD', 'SIMPLE', 1),
                                                                                     ('Sandwich',      '4901234567891', 5.99,  'USD', 'SIMPLE', 1),
                                                                                     ('USB Cable',     '4901234567892', 12.99, 'USD', 'SIMPLE', 2),
                                                                                     ('T-Shirt',       '4901234567893', 19.99, 'USD', 'SIMPLE', 3),
                                                                                     ('Lunch Bundle',  '4901234567894', 7.99,  'USD', 'BUNDLE', 1);