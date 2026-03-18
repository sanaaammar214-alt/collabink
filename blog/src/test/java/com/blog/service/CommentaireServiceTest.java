package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Commentaire;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.CommentaireRepository;
import com.blog.repository.LikeCommentaireRepository;
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
class CommentaireServiceTest {

    @Mock
    private CommentaireRepository commentaireRepository;

    @Mock
    private LikeCommentaireRepository likeCommentaireRepository;

    @InjectMocks
    private CommentaireService commentaireService;

    @Test
    void getById_idInexistant_doitLancerException() {
        when(commentaireRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> commentaireService.getById(99L));
        verify(commentaireRepository).findById(99L);
    }

    @Test
    void delete_proprietaire_doitSupprimer() {
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.LECTEUR);

        Commentaire c = new Commentaire();
        c.setId(10L);
        c.setUser(owner);

        when(commentaireRepository.findById(10L)).thenReturn(Optional.of(c));

        commentaireService.delete(10L, owner);

        verify(commentaireRepository).deleteById(10L);
    }

    @Test
    void delete_nonProprietaireNonAdmin_doitLancerSecurityException() {
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.LECTEUR);

        User autre = new User();
        autre.setId(2L);
        autre.setRole(Role.LECTEUR);

        Commentaire c = new Commentaire();
        c.setId(10L);
        c.setUser(owner);

        when(commentaireRepository.findById(10L)).thenReturn(Optional.of(c));

        assertThrows(SecurityException.class,
                () -> commentaireService.delete(10L, autre));

        verify(commentaireRepository, never()).deleteById(anyLong());
    }
}
