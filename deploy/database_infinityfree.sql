-- ====================================================================
-- SyncMeet — schema SOMENTE DAS TABELAS (versão InfinityFree)
--
-- Use ESTE arquivo (e não o database.sql principal) no phpMyAdmin da
-- InfinityFree: lá o banco `if0_43106594_syncmeet` já existe (foi
-- criado pelo painel) e a conta não tem permissão para CREATE DATABASE.
--
-- Como importar:
--   Control Panel → phpMyAdmin → clique no banco
--   `if0_43106594_syncmeet` na coluna esquerda → aba "Importar" →
--   selecione este arquivo → Executar.
-- ====================================================================

-- --------------------------------------------------------------------
-- Usuários do app
-- senha = hash gerado por password_hash() (PHP) — NUNCA texto puro
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id     INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email  VARCHAR(190) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    senha  VARCHAR(255) NOT NULL,
    UNIQUE KEY uq_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------------------
-- Eventos
-- hora_evento é VARCHAR(5) ("HH:mm") de propósito: o app envia e lê
-- exatamente esse formato; uma coluna TIME devolveria "HH:MM:SS" e
-- mudaria o que o app exibe.
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS eventos (
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    usuario_id   INT UNSIGNED NOT NULL,
    nome_evento  VARCHAR(150) NOT NULL,
    local_evento VARCHAR(200) NOT NULL,
    data_evento  DATE NOT NULL,
    hora_evento  VARCHAR(5) NOT NULL,
    CONSTRAINT fk_eventos_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
