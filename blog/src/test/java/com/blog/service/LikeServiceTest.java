package com.blog.service;

import com.blog.model.Article;
import com.blog.model.Like;
import com.blog.model.User;
import com.blog.repository.LikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    void toggleLike_pasEncoreLike_doitSauvegarder() {
        // Mocker findByUserIdAndArticleId(1L,1L) → Optional.empty()
        User user = new User();
        user.setId(1L);

        Article article = new Article();
        article.setId(1L);

        when(likeRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());

        // Appeler likeService.toggleLike(user, article)
        boolean result = likeService.toggleLike(user, article);

        // Vérifier verify(likeRepository).save(any(Like.class))
        verify(likeRepository).save(any(Like.class));

        // Vérifier assertThat(result).isTrue()
        assertThat(result).isTrue();
    }

    @Test
    void toggleLike_dejaLike_doitSupprimer() {
        // Mocker findByUserIdAndArticleId → Optional.of(existingLike)
        User user = new User();
        user.setId(1L);

        Article article = new Article();
        article.setId(1L);

        Like existingLike = new Like();
        existingLike.setUser(user);
        existingLike.setArticle(article);

        when(likeRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(existingLike));

        // Appeler likeService.toggleLike(user, article)
        boolean result = likeService.toggleLike(user, article);

        // Vérifier verify(likeRepository).delete(existingLike)
        verify(likeRepository).delete(existingLike);

        // Vérifier assertThat(result).isFalse()
        assertThat(result).isFalse();
    }
}
