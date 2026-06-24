-- Migration: Agregar campos adicionales a la tabla users para auditoría y información de usuario
-- Date: 2026-06-24

-- Verificar si la tabla existe y agregar las columnas si no existen
ALTER TABLE users ADD COLUMN IF NOT EXISTS nombre_completo VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS activo BOOLEAN DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS fecha_ultima_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS creado_por VARCHAR(255);

-- Crear índice para búsquedas por email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Crear índice para búsquedas por rol
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Crear índice para usuarios activos
CREATE INDEX IF NOT EXISTS idx_users_activo ON users(activo);

