package com.blog.service;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Inscrit un nouvel utilisateur avec validations et sécurité.
     * Validations : email non vide, email unique, password >= 6 chars,
     * nom non vide. Bloque la création d'un compte ADMIN (forcé LECTEUR).
     * Hache le mot de passe avec BCryptPasswordEncoder avant sauvegarde.
     * @param user utilisateur à inscrire
     * @throws IllegalArgumentException si une validation échoue
     */
    @Transactional
    public void inscrire(User user) {
        // Validations
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email obligatoire.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé.");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mot de passe minimum 6 caractères.");
        }

        if (user.getNom() == null || user.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }

        // Sécurité : interdire création d'ADMIN via register
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            user.setRole(Role.LECTEUR);
        }

        // Hachage BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    /**
     * Réservé à l'ADMIN. Modifie le rôle d'un utilisateur existant.
     * @param id identifiant de l'utilisateur cible
     * @param role nouveau rôle à attribuer
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public void changerRole(Long id, Role role) {
        User user = getById(id);
        user.setRole(role);
        userRepository.save(user);
    }

    /**
     * Supprime l'utilisateur et toutes ses données associées
     * via ON DELETE CASCADE (articles, commentaires, likes).
     * @param id identifiant de l'utilisateur à supprimer
     */
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
