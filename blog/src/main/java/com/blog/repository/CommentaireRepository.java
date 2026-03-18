package com.blog.repository;

import com.blog.model.Commentaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    // Commentaires d'un article triés du plus récent au plus ancien (uniquement les racines)
    @Query("SELECT c FROM Commentaire c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.reponses r LEFT JOIN FETCH r.user WHERE c.article.id = :articleId AND c.parent IS NULL ORDER BY c.dateCommentaire DESC")
    List<Commentaire> findTopLevelByArticleIdOrderByDateDesc(Long articleId);

    // Commentaires racines avec pagination
    @Query(value = "SELECT DISTINCT c FROM Commentaire c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.likes lc " +
           "LEFT JOIN FETCH lc.user " +
           "LEFT JOIN FETCH c.reponses r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.likes rl " +
           "LEFT JOIN FETCH rl.user " +
           "WHERE c.article.id = :articleId " +
           "AND c.parent IS NULL " +
           "ORDER BY c.dateCommentaire DESC",
           countQuery = "SELECT COUNT(c) FROM Commentaire c " +
           "WHERE c.article.id = :articleId AND c.parent IS NULL")
    Page<Commentaire> findRootByArticleId(Long articleId, Pageable pageable);
    
    // Charger les réponses d'un commentaire avec leurs utilisateurs
    @Query("SELECT c FROM Commentaire c LEFT JOIN FETCH c.reponses r LEFT JOIN FETCH r.user WHERE c.id = :commentaireId")
    Optional<Commentaire> findByIdWithReponses(Long commentaireId);
}
