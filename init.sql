-- Crear base de datos para user-service
CREATE DATABASE IF NOT EXISTS user_service_db;
-- Crear base de datos para account-service
CREATE DATABASE IF NOT EXISTS account_service_db;
-- Crear base de datos para auth-service
CREATE DATABASE IF NOT EXISTS auth_service_db;

-- Crear usuario para user-service
CREATE USER IF NOT EXISTS 'user_service_user'@'%' IDENTIFIED BY 'nerea';
GRANT ALL PRIVILEGES ON user_service_db.* TO 'user_service_user'@'%';

-- Crear usuario para account-service
CREATE USER IF NOT EXISTS 'account_service_user'@'%' IDENTIFIED BY 'nerea';
GRANT ALL PRIVILEGES ON account_service_db.* TO 'account_service_user'@'%';

-- Crear usuario para auth-service
CREATE USER IF NOT EXISTS 'auth_service_user'@'%' IDENTIFIED BY 'nerea';
GRANT ALL PRIVILEGES ON auth_service_db.* TO 'auth_service_user'@'%';


-- Aplicar cambios
FLUSH PRIVILEGES;
