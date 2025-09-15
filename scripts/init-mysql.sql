-- Criação de usuário adicional para a aplicação
CREATE USER IF NOT EXISTS 'xunim'@'%' IDENTIFIED BY 'xunim123';
GRANT ALL PRIVILEGES ON xunimpay.* TO 'xunim'@'%';
FLUSH PRIVILEGES;

-- Configurações de performance
SET GLOBAL innodb_buffer_pool_size = 134217728; -- 128MB
SET GLOBAL max_connections = 200;