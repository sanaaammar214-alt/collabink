package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Commentaire;
import com.blog.model.LikeCommentaire;
import com.blog.model.User;
import com.blog.repository.CommentaireRepository;
import com.blog.repository.LikeCommentaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentaireService {

    private final CommentaireRepository commentaireRepository;
    private final LikeCommentaireRepository likeCommentaireRepository;

    @Transactional
    public void save(Commentaire commentaire) {
        commentaireRepository.save(commentaire);
    }

    public Commentaire getById(Long id) {
        return commentaireRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commentaire introuvable : " + id));
    }

    public List<Commentaire> getByArticle(Long articleId) {
        return commentaireRepository.findTopLevelByArticleIdOrderByDateDesc(articleId);
    }

    public Page<Commentaire> getByArticlePaginated(Long articleId, int page) {
        return commentaireRepository.findRootByArticleId(
            articleId, PageRequest.of(page, 10));
    }

    @Transactional
    public void toggleLike(User user, Commentaire commentaire) {
        Optional<LikeCommentaire> existing = likeCommentaireRepository.findByUserIdAndCommentaireId(user.getId(), commentaire.getId());
        if (existing.isPresent()) {
            likeCommentaireRepository.delete(existing.get());
        } else {
            LikeCommentaire like = new LikeCommentaire();
            like.setUser(user);
            like.setCommentaire(commentaire);
            likeCommentaireRepository.save(like);
        }
    }

    public boolean hasLiked(Long userId, Long commentId) {
        return likeCommentaireRepository.existsByUserIdAndCommentaireId(userId, commentId);
    }

    // Vérifie propriétaire ou admin avant suppression
    @Transactional
    public void delete(Long id, User currentUser) {
        Commentaire c = getById(id);
        boolean isOwner = c.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new SecurityException("Vous n'êtes pas autorisé à supprimer ce commentaire.");
        }
        commentaireRepository.deleteById(id);
    }
}
