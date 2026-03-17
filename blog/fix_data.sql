USE collabink;

-- Remettre statut PUBLIE sur tous les articles NULL
UPDATE article SET statut = 'PUBLIE' WHERE statut IS NULL OR statut = '';

-- Vérifier les comptes (recréer si manquants)
INSERT IGNORE INTO users (nom, email, password, role, conditions_acceptees)
VALUES
('Administrateur','admin@collabink.ma',
 '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8bLdN6Vv7ZxKbJ4m7Yi',
 'ADMIN', true),
('Sara Ammar','sara@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'AUTEUR', true),
('Sanaa Ammar','sanaa@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWige.',
 'AUTEUR', true),
('Meriem','meriem@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'AUTEUR', true),
('Salma','salma@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'AUTEUR', true),
('Ahmed','ahmed@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'LECTEUR', false),
('Ali','ali@collabink.ma',
 '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
 'LECTEUR', false);

-- Recréer les catégories si manquantes
INSERT IGNORE INTO categorie (nom) VALUES
('Technologie'),('Culture'),('Science'),('Société'),('Art & Design');

-- Afficher ce qui reste en base pour diagnostic
SELECT COUNT(*) AS nb_articles FROM article;
SELECT COUNT(*) AS nb_users FROM users;
SELECT COUNT(*) AS nb_categories FROM categorie;
