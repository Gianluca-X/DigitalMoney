CREATE DATABASE IF NOT EXISTS account_service_db;

USE account_service_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    alias VARCHAR(255),
    available_amount DECIMAL(10, 2),
    cvu VARCHAR(255),
    user_id INT,
);