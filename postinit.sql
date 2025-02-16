-- Crear base de datos para Keycloak
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db') THEN
        CREATE DATABASE keycloak_db;
    END IF;
END
$$;

-- Crear usuario para Keycloak
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'keycloak_user') THEN
        CREATE ROLE keycloak_user WITH LOGIN PASSWORD 'securepassword';
    END IF;
END
$$;

-- Conceder privilegios al usuario en la base de datos
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak_user;
