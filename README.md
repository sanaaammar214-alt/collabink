# CollabInk — Blog Collaboratif

Application web de blog collaboratif développée avec Spring Boot 3, Spring Security, Thymeleaf et MySQL.

## Stack technique
- Java 21 + Spring Boot 3
- Spring Security (authentification par formulaire, rôles LECTEUR / AUTEUR / ADMIN)
- Spring Data JPA + MySQL 8
- Thymeleaf (templates HTML)
- Bootstrap 5 + Bootstrap Icons

## Prérequis
- Java 21+
- MySQL 8+
- Maven 3.8+

## Installation

1. Créer la base de données :
```sql
CREATE DATABASE blog_collaboratif CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Ou utiliser le fichier fourni : `blog/init.sql`

2. Configurer les variables d'environnement (ou modifier `application.properties`) :
```bash
DB_URL=jdbc:mysql://localhost:3306/blog_collaboratif?useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=votre_mot_de_passe
UPLOAD_DIR=uploads/images
```

3. Lancer l'application :
```bash
cd blog
mvn spring-boot:run
```

4. Accéder à : http://localhost:8080

## Fonctionnalités
- ✅ Inscription / Connexion / Déconnexion
- ✅ Création, modification, suppression d'articles (AUTEUR/ADMIN)
- ✅ Upload d'image de couverture (JPEG, PNG, GIF, WebP — max 5 Mo)
- ✅ Catégories
- ✅ Commentaires (authentifiés)
- ✅ Système de likes (toggle)
- ✅ Recherche d'articles
- ✅ Pagination
- ✅ Panel d'administration (gestion users, articles, catégories)
- ✅ Gestion des rôles par l'admin

## Structure des uploads
Les images uploadées sont stockées dans `{user.dir}/uploads/images/` et servies via l'URL `/uploads/images/{filename}`.

## Comptes de test

Les comptes de démonstration sont créés automatiquement au premier démarrage
par le `DataInitializer`. Consultez ce fichier pour les identifiants,
ou référez-vous au fichier `.env.example` pour la configuration.

> ⚠️ Ne jamais publier de mots de passe réels dans un README public.
