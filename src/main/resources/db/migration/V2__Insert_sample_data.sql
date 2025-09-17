-- Insert example users
INSERT INTO users (email, document, full_name, phone) VALUES
                                                          ('joao.silva@email.com', '12345678901', 'João Silva', '11999999999'),
                                                          ('maria.santos@email.com', '98765432100', 'Maria Santos', '11888888888'),
                                                          ('admin@xunimpay.com', '11111111111', 'Admin XunimPay', '1133333333');

-- Insert example accounts
INSERT INTO accounts (user_id, account_number, balance) VALUES
                                                            (1, '001-123456-7', 1000.00),
                                                            (2, '001-789012-3', 2500.50),
                                                            (3, '001-999999-9', 10000.00);

-- Insert example payments
INSERT INTO payments (from_account_id, to_account_id, amount, payment_type, status, description, transaction_id) VALUES
                                                                                                                     (1, 2, 100.00, 'PIX', 'COMPLETED', 'Pagamento teste PIX', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-1')),
                                                                                                                     (2, 1, 50.00, 'TRANSFER', 'COMPLETED', 'Transferência entre contas', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-2')),
                                                                                                                     (3, 1, 500.00, 'PIX', 'PENDING', 'Pagamento administrativo', CONCAT('TXN-', UNIX_TIMESTAMP()*1000, '-3'));

-- Update balances after payments
UPDATE accounts SET balance = balance - 100.00 WHERE id = 1; -- João pagou 100 para Maria
UPDATE accounts SET balance = balance + 100.00 WHERE id = 2; -- Maria recebeu 100 de João
UPDATE accounts SET balance = balance - 50.00 WHERE id = 2;  -- Maria pagou 50 para João
UPDATE accounts SET balance = balance + 50.00 WHERE id = 1;  -- João recebeu 50 de Maria