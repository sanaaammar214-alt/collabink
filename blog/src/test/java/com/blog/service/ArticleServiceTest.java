package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Article;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategorieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategorieRepository categorieRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void save_doitGenererUnSlug() {
        // Créer un Article avec titre "Mon Super Article"
        Article article = new Article();
        article.setTitre("Mon Super Article");
        article.setContenu("Contenu de test suffisant pour valider les contraintes".repeat(2));

        // Appeler articleService.save(article)
        articleService.save(article);

        // Vérifier que article.getSlug() contient "mon-super-article"
        assertThat(article.getSlug()).contains("mon-super-article");
        verify(articleRepository).save(article);
    }

    @Test
    void getById_idInexistant_doitLancerException() {
        // Mocker articleRepository.findByIdWithDetails(99L) → Optional.empty()
        when(articleRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        // Vérifier que assertThrows(ResourceNotFoundException.class) est levée
        assertThrows(ResourceNotFoundException.class, () -> articleService.getById(99L));
        verify(articleRepository).findByIdWithDetails(99L);
    }

    @Test
    void peutEditer_auteurDeLarticle_doitRetournerTrue() {
        // Créer User id=1 rôle AUTEUR, Article avec cet auteur
        User auteur = new User();
        auteur.setId(1L);
        auteur.setRole(Role.AUTEUR);

        Article article = new Article();
        article.setAuteur(auteur);

        // Vérifier que peutEditer(article, auteur) retourne true
        assertThat(articleService.peutEditer(article, auteur)).isTrue();
    }

    @Test
    void peutEditer_autreUtilisateur_doitRetournerFalse() {
        // Créer User auteur id=1, autre User id=2 rôle LECTEUR
        User auteur = new User();
        auteur.setId(1L);
        auteur.setRole(Role.AUTEUR);

        User autre = new User();
        autre.setId(2L);
        autre.setRole(Role.LECTEUR);

        Article article = new Article();
        article.setAuteur(auteur);

        // Vérifier que peutEditer(article, autre) retourne false
        assertThat(articleService.peutEditer(article, autre)).isFalse();
    }
}
