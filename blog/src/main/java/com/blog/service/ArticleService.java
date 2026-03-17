package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Article;
import com.blog.model.Categorie;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategorieRepository categorieRepository;

    public List<Article> getAll() {
        return sanitizeImages(articleRepository.findAllWithDetails());
    }

    /**
     * Récupère les articles avec pagination pour éviter le problème N+1 Hibernate.
     * Utilise LEFT JOIN FETCH pour charger les associations.
     * @param page numéro de page (commence à 0)
     * @param size nombre d'articles par page
     * @return Page d'articles triés par date de publication décroissante
     */
    public Page<Article> getAllPaginated(int page, int size) {
        Page<Article> result = articleRepository.findAllByOrderByDatePublicationDesc(
                PageRequest.of(page, size, Sort.by("datePublication").descending())
        );
        sanitizeImages(result.getContent());
        return result;
    }

    public List<Article> getAllOrderByLikes() {
        return sanitizeImages(articleRepository.findAllOrderByLikesDesc());
    }

    public Article getById(Long id) {
        Article article = articleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + id));
        return sanitizeImage(article);
    }

    public List<Article> getByCategorie(Long categorieId) {
        return sanitizeImages(articleRepository.findByCategorieIdWithDetails(categorieId));
    }

    public List<Article> getByAuteur(Long auteurId) {
        return sanitizeImages(articleRepository.findByAuteurIdWithDetails(auteurId));
    }

    public List<Article> search(String keyword) {
        return sanitizeImages(articleRepository.searchByKeyword(keyword));
    }

    /**
     * Persiste un article en générant automatiquement son slug si nécessaire.
     * @param article l'article à persister (slug généré automatiquement)
     * @throws IllegalArgumentException si titre null ou vide
     */
    @Transactional
    public void save(Article article) {
        if (article.getSlug() == null || article.getSlug().isBlank()) {
            article.setSlug(generateSlug(article.getTitre()));
        }
        articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable : " + id));

        if (article.getImage() != null && !article.getImage().isBlank()) {
            Path imagePath = Paths.get(System.getProperty("user.dir"), "uploads/images", article.getImage());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException ignored) {
            }
        }

        articleRepository.deleteById(id);
    }

    /**
     * Vérifie si l'utilisateur peut modifier l'article.
     * Règle : uniquement l'auteur de l'article
     * @return true si l'utilisateur est l'auteur de l'article
     */
    public boolean peutEditer(Article article, User user) {
        return article.getAuteur().getId().equals(user.getId());
    }

    public boolean peutSupprimer(Article article, User user) {
        return article.getAuteur().getId().equals(user.getId())
                || user.getRole() == Role.ADMIN;
    }

    public Categorie getCategorieById(Long id) {
        return categorieRepository.findById(id).orElse(null);
    }

    public long countArticlesByAuteur(Long auteurId) {
        return articleRepository.countByAuteurId(auteurId);
    }

    /**
     * Analyse les catégories des articles likés par l'utilisateur,
     * retourne des articles non encore likés dans ces catégories.
     * @param user utilisateur connecté
     * @return liste max 6 articles, vide si aucun like
     */
    public List<Article> getRecommendations(User user) {
        if (user == null || user.getLikes() == null || user.getLikes().isEmpty()) {
            return List.of();
        }

        List<Long> categorieIds = user.getLikes().stream()
                .map(l -> l.getArticle() != null && l.getArticle().getCategorie() != null ? l.getArticle().getCategorie().getId() : null)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (categorieIds.isEmpty()) {
            return List.of();
        }

        return sanitizeImages(articleRepository.findRecommendedByCategories(categorieIds, user.getId()))
                .stream()
                .limit(6)
                .toList();
    }

    private List<Article> sanitizeImages(List<Article> articles) {
        if (articles == null || articles.isEmpty()) return articles;
        for (Article a : articles) {
            sanitizeImage(a);
        }
        return articles;
    }

    private Article sanitizeImage(Article article) {
        if (article == null) return null;
        if (article.getImage() == null || article.getImage().isBlank()) return article;

        Path imagePath = Paths.get(System.getProperty("user.dir"), "uploads/images", article.getImage());
        if (!Files.exists(imagePath)) {
            article.setImage(null);
        }
        return article;
    }

    /**
     * Transforme "Mon Titre !" en "mon-titre-1748293847"
     * @param titre le titre de l'article
     * @return slug unique en minuscules avec suffixe timestamp
     */
    private String generateSlug(String titre) {
        if (titre == null) {
            return "article-" + System.currentTimeMillis();
        }
        String base = titre.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");
        if (base.isBlank()) {
            base = "article";
        }
        return base + "-" + System.currentTimeMillis();
    }
}