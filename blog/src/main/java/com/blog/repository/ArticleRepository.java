package com.blog.repository;

import com.blog.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Fetch complet pour éviter LazyInitializationException
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY a.datePublication DESC
        """)
    List<Article> findAllWithDetails();

    // Fetch complet pour un seul article
    @Query("""
        SELECT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires c
        LEFT JOIN FETCH c.user
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE a.id = :id
        """)
    Optional<Article> findByIdWithDetails(Long id);

    // Tri par nombre de likes
    @Query("""
        SELECT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY SIZE(a.likes) DESC
        """)
    List<Article> findAllOrderByLikesDesc();

    // Filtre par catégorie
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE a.categorie.id = :categorieId AND (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY a.datePublication DESC
        """)
    List<Article> findByCategorieIdWithDetails(Long categorieId);

    // Filtre par auteur
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE a.auteur.id = :auteurId
        ORDER BY a.datePublication DESC
        """)
    List<Article> findByAuteurIdWithDetails(Long auteurId);

    // Recherche full-text
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE (LOWER(a.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(a.contenu) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY a.datePublication DESC
        """)
    List<Article> searchByKeyword(String keyword);

    // Articles recommandés (par catégories aimées)
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE a.categorie.id IN :categorieIds
        AND a.id NOT IN (SELECT l.article.id FROM Like l WHERE l.user.id = :userId)
        AND (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY a.datePublication DESC
    """)
    List<Article> findRecommendedByCategories(List<Long> categorieIds, Long userId);

    // Compter les articles d'un auteur
    long countByAuteurId(Long auteurId);

    // Pagination
    Page<Article> findAllByOrderByDatePublicationDesc(Pageable pageable);
    
    // Pagination avec JOIN FETCH pour éviter LazyInitializationException
    @Query("""
        SELECT DISTINCT a FROM Article a
        LEFT JOIN FETCH a.likes
        LEFT JOIN FETCH a.commentaires
        LEFT JOIN FETCH a.categorie
        LEFT JOIN FETCH a.auteur
        WHERE (a.statut = 'PUBLIE' OR a.statut IS NULL)
        ORDER BY a.datePublication DESC
        """)
    List<Article> findAllWithDetailsPaginated(Pageable pageable);
}
