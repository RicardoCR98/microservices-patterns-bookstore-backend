-- Crear base de datos "UserAuth_DB"
CREATE DATABASE "UserAuth_DB";

-- Crear base de datos "Payments_DB"
CREATE DATABASE "Payments_DB";

-- Crear base de datos "Orders_DB"
CREATE DATABASE "Orders_DB";

-- Conectarse a la base de datos "UserAuth_DB"
\c "UserAuth_DB";

-- Crear tabla "auth_user" si no existe
CREATE TABLE IF NOT EXISTS auth_user (
    user_id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    updated_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT ukklvc3dss72qnlrjp2bai055mw UNIQUE (email)
    );

-- Insertar el usuario administrador si no existe
INSERT INTO auth_user (full_name, email, password_hash, role, is_active, created_at, updated_at, is_deleted)
VALUES ('Admin', 'admin@admin.com', '$2y$10$41MnYfanr5QfHcyTWxZaN.Z4jDA4V8JZAd9GLVTtoOd.U7/Xk1qmi', 'ADMIN', true, NOW(), NOW(), false)
    ON CONFLICT (email) DO NOTHING;
