-- ============================================================
-- CollabInk — Script d'initialisation de la base de données
-- MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS collabink
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE collabink;

-- ── Table USERS ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom      VARCHAR(100)  NOT NULL,
    email    VARCHAR(150)  NOT NULL UNIQUE,
    password VARCHAR(255)  NOT NULL,
    role     ENUM('ADMIN','AUTEUR','LECTEUR') NOT NULL DEFAULT 'LECTEUR',
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table CATEGORIES ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categorie (
    id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table ARTICLES ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS article (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre            VARCHAR(255) NOT NULL,
    contenu          TEXT         NOT NULL,
    image            VARCHAR(255),
    date_publication DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id          BIGINT       NOT NULL,
    categorie_id     BIGINT,
    INDEX idx_article_user     (user_id),
    INDEX idx_article_categorie (categorie_id),
    INDEX idx_article_date     (date_publication),
    CONSTRAINT fk_article_user      FOREIGN KEY (user_id)      REFERENCES users(id)     ON DELETE CASCADE,
    CONSTRAINT fk_article_categorie FOREIGN KEY (categorie_id) REFERENCES categorie(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table COMMENTAIRES ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS commentaire (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    contenu          TEXT   NOT NULL,
    date_commentaire DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    article_id       BIGINT NOT NULL,
    user_id          BIGINT NOT NULL,
    INDEX idx_commentaire_article (article_id),
    INDEX idx_commentaire_user    (user_id),
    CONSTRAINT fk_comment_article FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user    FOREIGN KEY (user_id)    REFERENCES users(id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Table LIKES ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    -- Contrainte UNIQUE : un utilisateur ne peut liker qu'une fois
    UNIQUE KEY uk_like_user_article (user_id, article_id),
    INDEX idx_like_article (article_id),
    CONSTRAINT fk_like_article FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    CONSTRAINT fk_like_user    FOREIGN KEY (user_id)    REFERENCES users(id)   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Données de test
-- ============================================================

-- Admin (password: admin123)
INSERT IGNORE INTO users (nom, email, password, role) VALUES
    ('Administrateur', 'admin@collabink.ma',
     '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8bLdN6Vv7ZxKbJ4m7Yi',
     'ADMIN');

-- Auteur (password: auteur123)
INSERT IGNORE INTO users (nom, email, password, role) VALUES
    ('Sara Ammar', 'sara@collabink.ma',
     '$2a$12$eImiTXuWVxfM37uY4JANjQ==PLACEHOLDER',
     'AUTEUR');

-- Lecteur (password: lecteur123)
INSERT IGNORE INTO users (nom, email, password, role) VALUES
    ('Mohammed Khalil', 'khalil@collabink.ma',
     '$2a$12$eImiTXuWVxfM37uY4JANjQ==PLACEHOLDER',
     'LECTEUR');

-- Catégories
INSERT IGNORE INTO categorie (nom) VALUES
    ('Technologie'),
    ('Culture'),
    ('Science'),
    ('Société'),
    ('Art & Design');

-- Index full-text pour la recherche
ALTER TABLE article ADD FULLTEXT INDEX ft_article_search (titre, contenu);
