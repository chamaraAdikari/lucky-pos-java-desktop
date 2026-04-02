-- Users table
CREATE TABLE users (
                       id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username    VARCHAR(50)  NOT NULL UNIQUE,
                       pin_hash    VARCHAR(255) NOT NULL,
                       role        VARCHAR(20)  NOT NULL,
                       active      BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin (PIN: 1234 — will be BCrypt hashed in Day 3)
INSERT INTO users (username, pin_hash, role)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');