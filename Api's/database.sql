-- ====================================================================
-- SyncMeet — schema do banco de dados MySQL
--
-- Como usar:
--   cPanel           → phpMyAdmin → banco criado → aba "SQL" → colar
--   VPS / linha cmd  → mysql -u USUARIO -p syncmeet < database.sql
--
-- O nome do banco deve ser "syncmeet" (ou o mesmo que estiver em
-- conexao.php). Charset utf8mb4 para acentuação correta.
-- ====================================================================

CREATE DATABASE IF NOT EXISTS syncmeet
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE syncmeet;

-- --------------------------------------------------------------------
-- Usuários do app
-- senha = hash gerado por password_hash() (PHP) — NUNCA texto puro
-- VARCHAR(255) comporta hashes atuais e futuros do PHP
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

-- Usuário de teste (opcional): descomente para criar.
-- Senha em texto puro: 123456 — troque/remova em produção.
-- INSERT INTO usuarios (email, usuario, senha) VALUES
-- ('teste@syncmeet.app', 'Teste',
--  '$2y$10$e0NRl8v5JWpB1s2hG3q4n.5f6G7h8I9j0K1l2M3n4o5P6q7R8s9T0u');
