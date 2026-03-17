package com.blog.service;

import com.blog.model.Article;
import com.blog.model.Like;
import com.blog.model.User;
import com.blog.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;

    /**
     * Logique toggle : si le like existe → supprime et retourne false.
     * Sinon → crée et retourne true. Contrainte UNIQUE (user,article)
     * garantie par @UniqueConstraint en base.
     * @param user utilisateur qui like/unlike
     * @param article article concerné
     * @return true si liké, false si déliké
     */
    @Transactional
    public boolean toggleLike(User user, Article article) {
        Optional<Like> existing = likeRepository.findByUserIdAndArticleId(user.getId(), article.getId());
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // unliked
        } else {
            Like like = new Like();
            like.setUser(user);
            like.setArticle(article);
            likeRepository.save(like);
            return true; // liked
        }
    }

    public long countLikes(Long articleId) {
        return likeRepository.countByArticleId(articleId);
    }

    public boolean hasLiked(Long userId, Long articleId) {
        return likeRepository.existsByUserIdAndArticleId(userId, articleId);
    }
}
