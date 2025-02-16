CREATE DATABASE IF NOT EXISTS user_service_db;

USE user_service_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    dni INT,
    email VARCHAR(255),
    firstname VARCHAR(255),
    lastname VARCHAR(255),
    password VARCHAR(255),
    phone VARCHAR(255)
);
