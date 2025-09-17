-- Users Table
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       document VARCHAR(14) UNIQUE NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       phone VARCHAR(15),
                       is_active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       INDEX idx_users_email (email),
                       INDEX idx_users_document (document),
                       INDEX idx_users_is_active (is_active)
);

-- Accounts Table
CREATE TABLE accounts (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          account_number VARCHAR(20) UNIQUE NOT NULL,
                          balance DECIMAL(15,2) DEFAULT 0.00,
                          status ENUM('ACTIVE', 'BLOCKED', 'CLOSED') DEFAULT 'ACTIVE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                          INDEX idx_accounts_user_id (user_id),
                          INDEX idx_accounts_number (account_number),
                          INDEX idx_accounts_status (status),
                          INDEX idx_accounts_balance (balance)
);

-- Payments Table
CREATE TABLE payments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          from_account_id BIGINT,
                          to_account_id BIGINT NOT NULL,
                          amount DECIMAL(15,2) NOT NULL,
                          payment_type ENUM('PIX', 'TRANSFER', 'CARD', 'BOLETO') NOT NULL,
                          status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
                          description TEXT,
                          external_id VARCHAR(255),
                          transaction_id VARCHAR(255) UNIQUE NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          processed_at TIMESTAMP NULL,
                          FOREIGN KEY (from_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                          FOREIGN KEY (to_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                          INDEX idx_payments_from_account (from_account_id),
                          INDEX idx_payments_to_account (to_account_id),
                          INDEX idx_payments_status (status),
                          INDEX idx_payments_type (payment_type),
                          INDEX idx_payments_transaction_id (transaction_id),
                          INDEX idx_payments_created_at (created_at),
                          INDEX idx_payments_processed_at (processed_at),
                          CONSTRAINT chk_payments_amount CHECK (amount > 0),
                          CONSTRAINT chk_payments_different_accounts CHECK (
                              from_account_id IS NULL OR from_account_id != to_account_id
)
    );