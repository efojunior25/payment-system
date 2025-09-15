-- insert example users
INSERT INTO users (email, document, full_name, phone) VALUES
    ('joao.silva@email.com', '12345678901', 'João Silva', '11999999999'),
    ('maria.santos@email.com', '98765432100', 'Maria Santos', '11888888888'),
    ('admin@magalupay.com', '11111111111', 'Admin MagaluPay', '1133333333');

-- insert example accounts
INSERT INTO accounts (user_id, account_number, balance) VALUES
    (1, '001-123456-7', 1000.00),
    (2, '001-789012-3', 2500.50),
    (3, '001-999999-9', 10000.00);

-- inserting example payments
INSERT INTO payments (from_account_id, to_account_id, amount, payment_type, status, description, transaction_id) VALUES
    (1, 2, 100.00, 'PIX', 'COMPLETED', 'Pagamento teste PIX', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-1')),
    (2, 1, 50.00, 'TRANSFER', 'COMPLETED', 'Transferência entre contas', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-2')),
    (3, 1, 500.00, 'PIX', 'PENDING', 'Pagamento administrativo', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-3'));