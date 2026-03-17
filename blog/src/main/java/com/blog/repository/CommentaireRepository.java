package com.blog.repository;

import com.blog.model.Commentaire;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    // Commentaires d'un article triés du plus récent au plus ancien (uniquement les racines)
    @Query("SELECT c FROM Commentaire c LEFT JOIN FETCH c.user WHERE c.article.id = :articleId AND c.parent IS NULL ORDER BY c.dateCommentaire DESC")
    List<Commentaire> findTopLevelByArticleIdOrderByDateDesc(Long articleId);

    // Commentaires racines avec pagination
    @Query("SELECT c FROM Commentaire c " +
           "WHERE c.article.id = :articleId " +
           "AND c.parent IS NULL " +
           "ORDER BY c.dateCommentaire DESC")
    Page<Commentaire> findRootByArticleId(Long articleId, Pageable pageable);
}
