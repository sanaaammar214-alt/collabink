package com.blog.controller;

import com.blog.model.Article;
import com.blog.model.Categorie;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.CategorieRepository;
import com.blog.repository.CommentaireRepository;
import com.blog.repository.LikeRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;
    private final CategorieRepository categorieRepository;
    private final CommentaireRepository commentaireRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    // ─── DASHBOARD ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("nbUsers",        userService.getAll().size());
        model.addAttribute("nbCommentaires", commentaireRepository.count());
        model.addAttribute("nbLikes",        likeRepository.count());
        
        // Un seul appel pour éviter le N+1, puis tri côté Java
        List<Article> allArticles = articleService.getAll();
        model.addAttribute("nbArticles", allArticles.size());
        model.addAttribute("dernierArticles", allArticles.stream().limit(5).toList());
        model.addAttribute("topArticles", allArticles.stream()
                .sorted((a1, a2) -> Integer.compare(a2.getLikes().size(), a1.getLikes().size()))
                .limit(5).toList());
        
        return "admin/dashboard";
    }

    // ─── UTILISATEURS ───────────────────────────────────────────────────────
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAll());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changerRole(@PathVariable Long id,
                              @RequestParam Role role,
                              RedirectAttributes redirectAttributes) {
        userService.changerRole(id, role);
        redirectAttributes.addFlashAttribute("successMessage", "Rôle mis à jour avec succès.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String supprimerUser(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal != null) {
            User current = userService.getAll().stream()
                    .filter(u -> u.getEmail().equals(principal.getName()))
                    .findFirst()
                    .orElse(null);
            if (current != null && current.getId().equals(id)) {
                throw new AccessDeniedException("Un administrateur ne peut pas se supprimer lui-même.");
            }
        }

        userService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprimé avec succès.");
        return "redirect:/admin/users";
    }

    // ─── CATÉGORIES ─────────────────────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categorieRepository.findAll());
        model.addAttribute("nouvelleCategorie", new Categorie());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategorie(@ModelAttribute Categorie categorie,
                                RedirectAttributes redirectAttributes) {
        categorieRepository.save(categorie);
        redirectAttributes.addFlashAttribute("successMessage", "Catégorie ajoutée avec succès.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategorie(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        categorieRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Catégorie supprimée avec succès.");
        return "redirect:/admin/categories";
    }

    // ─── ARTICLES (admin) ───────────────────────────────────────────────────
    @GetMapping("/articles")
    public String articles(Model model) {
        model.addAttribute("articles", articleService.getAll());
        return "admin/articles";
    }

    @PostMapping("/articles/{id}/delete")
    public String deleteArticle(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Article supprimé avec succès.");
        return "redirect:/admin/articles";
    }

    // ─── PAGE DE SUPPRESSION ADMIN AVEC RAISONS ─────────────────────────────────
    @GetMapping("/articles/{id}/supprimer")
    public String confirmDeleteArticle(@PathVariable Long id, Model model, Principal principal) {
        Article article = articleService.getById(id);
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        // Vérifier que c'est bien un admin qui supprime l'article d'un autre
        if (!admin.getRole().equals(Role.ADMIN) || admin.getId().equals(article.getAuteur().getId())) {
            return "redirect:/admin/articles";
        }

        model.addAttribute("article", article);
        model.addAttribute("admin", admin);
        
        // Raisons prédéfinies
        List<String> raisonsPredefinies = Arrays.asList(
            "Contenu inapproprié ou offensant",
            "Violation des conditions d'utilisation",
            "Contenu dupliqué ou plagié",
            "Informations incorrectes ou fausses",
            "Spam ou contenu promotionnel",
            "Demande de l'auteur original",
            "Violation de droits d'auteur",
            "Autre raison (à préciser)"
        );
        model.addAttribute("raisonsPredefinies", raisonsPredefinies);

        return "admin/suppression-article";
    }

    @PostMapping("/articles/{id}/supprimer-confirmer")
    public String confirmDeleteArticle(@PathVariable Long id,
                                      @RequestParam String raisonSuppression,
                                      @RequestParam(required = false) String raisonAutre,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        Article article = articleService.getById(id);
        User admin = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Admin non trouvé"));

        // Vérifier les permissions
        if (!admin.getRole().equals(Role.ADMIN) || admin.getId().equals(article.getAuteur().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action non autorisée.");
            return "redirect:/admin/articles";
        }

        // Construire la raison finale
        String raisonFinale = raisonSuppression;
        if ("Autre raison (à préciser)".equals(raisonSuppression) && raisonAutre != null && !raisonAutre.isBlank()) {
            raisonFinale = "Autre: " + raisonAutre;
        }

        if (raisonFinale == null || raisonFinale.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "La raison de suppression est obligatoire.");
            return "redirect:/admin/articles/" + id + "/supprimer";
        }

        // Enregistrer la raison et supprimer
        article.setRaisonSuppression(raisonFinale);
        articleService.save(article);
        articleService.delete(id);

        redirectAttributes.addFlashAttribute("successMessage", 
            String.format("Article de %s supprimé. Raison: %s", article.getAuteur().getNom(), raisonFinale));
        return "redirect:/admin/articles";
    }
}
