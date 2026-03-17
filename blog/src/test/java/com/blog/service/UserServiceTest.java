package com.blog.service;

import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void inscrire_donneesValides_doitSauvegarder() {
        // User(nom="Sanaa", email="sanaa@test.ma", password="motdepasse123")
        User user = new User();
        user.setNom("Sanaa");
        user.setEmail("sanaa@test.ma");
        user.setPassword("motdepasse123");

        // Mocker existsByEmail → false, passwordEncoder.encode → "$2a$hash"
        when(userRepository.existsByEmail("sanaa@test.ma")).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("$2a$hash");

        // Appeler inscrire
        userService.inscrire(user);

        // Vérifier verify(userRepository).save(user)
        verify(userRepository).save(user);

        // Vérifier user.getRole() == Role.LECTEUR
        assertThat(user.getRole()).isEqualTo(Role.LECTEUR);
    }

    @Test
    void inscrire_emailExistant_doitLancerException() {
        // Mocker existsByEmail("sara@collabink.ma") → true
        when(userRepository.existsByEmail("sara@collabink.ma")).thenReturn(true);

        User user = new User();
        user.setEmail("sara@collabink.ma");

        // Vérifier assertThrows(IllegalArgumentException.class)
        assertThrows(IllegalArgumentException.class, () -> userService.inscrire(user));
    }

    @Test
    void inscrire_roleAdmin_doitForcerLecteur() {
        // Créer User avec role = Role.ADMIN
        User user = new User();
        user.setNom("Test");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setRole(Role.ADMIN);

        // Mocker existsByEmail → false, passwordEncoder → "hash"
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hash");

        // Appeler inscrire
        userService.inscrire(user);

        // Vérifier qu'après inscrire(), user.getRole() == Role.LECTEUR
        assertThat(user.getRole()).isEqualTo(Role.LECTEUR);
    }
}

