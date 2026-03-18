package com.blog.controller;

import com.blog.exception.ResourceNotFoundException;
import com.blog.model.Article;
import com.blog.model.ArticleStatut;
import com.blog.model.Commentaire;
import com.blog.model.Role;
import com.blog.model.User;
import com.blog.repository.CategorieRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import com.blog.service.CommentaireService;
import com.blog.service.FileUploadService;
import com.blog.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private static final int PAGE_SIZE = 6;

    private final ArticleService articleService;
    private final CommentaireService commentaireService;
    private final LikeService likeService;
    private final CategorieRepository categorieRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    // ─── LISTE avec pagination ───────────────────────────────────────────────
    @GetMapping
    public String liste(@RequestParam(required = false) Long categorieId,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {

        if (q != null && !q.isBlank()) {
            model.addAttribute("articles", articleService.search(q));
            model.addAttribute("searchQuery", q);
        } else if ("likes".equals(sort)) {
            model.addAttribute("articles", articleService.getAllOrderByLikes());
        } else if (categorieId != null) {
            model.addAttribute("articles", articleService.getByCategorie(categorieId));
            model.addAttribute("categorieSelectionnee", categorieId);
        } else {
            Page<Article> articlesPage = articleService.getAllPaginated(page, PAGE_SIZE);
            model.addAttribute("articles", articlesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", articlesPage.getTotalPages());
            model.addAttribute("totalItems", articlesPage.getTotalElements());
        }

        model.addAttribute("sortSelectionne", sort);
        return "articles/liste";
    }

    // ─── LISTE MES ARTICLES ────────────────────────────────────────────────
    @GetMapping("/mes-articles")
    public String mesArticles(Principal principal, Model model) {
        User user = getUser(principal);
        model.addAttribute("articles", articleService.getByAuteur(user.getId()));
        return "articles/mes-articles";
    }

    // ─── DETAIL ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Transactional
    public String detail(@PathVariable Long id, 
                        @RequestParam(defaultValue="0") int commentPage,
                        Model model, Principal principal) {
        Article article = articleService.getById(id);
        
        var commentairesPage = commentaireService.getByArticlePaginated(id, commentPage);
        model.addAttribute("article", article);
        model.addAttribute("commentaires", commentairesPage.getContent());
        model.addAttribute("commentPage", commentPage);
        model.addAttribute("totalCommentPages", commentairesPage.getTotalPages());
        model.addAttribute("nbLikes", article.getLikes().size());
        model.addAttribute("nbArticlesAuteur", articleService.countArticlesByAuteur(article.getAuteur().getId()));

        if (principal != null) {
            User user = getUser(principal);
            model.addAttribute("aLike", likeService.hasLiked(user.getId(), id));
        }
        return "articles/detail";
    }

    // ─── FORMULAIRE CRÉATION ────────────────────────────────────────────────
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("article", new Article());
        return "articles/form";
    }

    // ─── SAVE (création) ────────────────────────────────────────────────────
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Article article,
                       BindingResult result,
                       @RequestParam(required = false) Long categorieId,
                       @RequestParam(required = false) MultipartFile imageFile,
                       @RequestParam(defaultValue="PUBLIE") String statut,
                       Principal principal,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "articles/form";
        }

        try {
            User auteur = getUser(principal);
            article.setAuteur(auteur);
            article.setDatePublication(LocalDateTime.now());
            article.setStatut(ArticleStatut.valueOf(statut));

            if (categorieId != null) {
                article.setCategorie(articleService.getCategorieById(categorieId));
            }

            String filename = fileUploadService.uploadImage(imageFile);
            if (filename != null) {
                article.setImage(filename);
            }

            articleService.save(article);
        } catch (IOException e) {
            model.addAttribute("error", "Échec de l'upload de l'image : " + e.getMessage());
            return "articles/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Article publié avec succès !");
        return "redirect:/articles";
    }

    // ─── FORMULAIRE EDITION ─────────────────────────────────────────────────
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Article article = articleService.getById(id);
        User user = getUser(principal);

        if (!articleService.peutEditer(article, user)) {
            throw new AccessDeniedException("Vous n'êtes pas autorisé à modifier cet article.");
        }

        model.addAttribute("article", article);
        return "articles/form";
    }

    // ─── UPDATE ─────────────────────────────────────────────────────────────
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Article articleUpdate,
                         BindingResult result,
                         @RequestParam(required = false) Long categorieId,
                         @RequestParam(required = false) MultipartFile imageFile,
                         @RequestParam(defaultValue="PUBLIE") String statut,
                         Principal principal,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        Article article = articleService.getById(id);
        User user = getUser(principal);

        if (!articleService.peutEditer(article, user)) {
            throw new AccessDeniedException("Accès refusé.");
        }

        if (result.hasErrors()) {
            model.addAttribute("article", article);
            return "articles/form";
        }

        try {
            article.setTitre(articleUpdate.getTitre());
            article.setContenu(articleUpdate.getContenu());
            article.setStatut(ArticleStatut.valueOf(statut));

            if (categorieId != null) {
                article.setCategorie(articleService.getCategorieById(categorieId));
            } else {
                article.setCategorie(null);
            }

            String filename = fileUploadService.uploadImage(imageFile);
            if (filename != null) {
                article.setImage(filename);
            }

            articleService.save(article);
        } catch (IOException e) {
            model.addAttribute("error", "Échec de l'upload de l'image : " + e.getMessage());
            model.addAttribute("article", article);
            return "articles/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Article mis à jour avec succès !");
        return "redirect:/articles/" + id;
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) String raisonSuppression,
                         @RequestParam(required = false) String redirect,
                         Principal principal) {

        Article article = articleService.getById(id);
        User user = getUser(principal);

        if (!articleService.peutSupprimer(article, user)) {
            throw new AccessDeniedException("Accès refusé.");
        }

        // Admin qui supprime l'article d'un autre → raison obligatoire
        boolean isAdminSupprimantAutrui =
                user.getRole() == Role.ADMIN &&
                !article.getAuteur().getId().equals(user.getId());

        if (isAdminSupprimantAutrui) {
            if (raisonSuppression == null || raisonSuppression.isBlank()) {
                // Rediriger avec erreur si raison manquante
                return "redirect:/articles/" + id + "?erreurSuppression=true";
            }
            article.setRaisonSuppression(raisonSuppression);
            articleService.save(article);
        }

        articleService.delete(id);

        if ("mes-articles".equals(redirect)) {
            return "redirect:/articles/mes-articles";
        }
        if ("admin".equals(redirect)) {
            return "redirect:/admin/articles";
        }
        return "redirect:/articles";
    }

    // ─── LIKE (toggle) ──────────────────────────────────────────────────────
    @PostMapping("/{id}/like")
    public String like(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);
        Article article = articleService.getById(id);
        likeService.toggleLike(user, article);
        return "redirect:/articles/" + id;
    }

    // ─── COMMENTAIRE ────────────────────────────────────────────────────────
    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String contenu,
                             Principal principal) {
        if (contenu == null || contenu.isBlank()) {
            return "redirect:/articles/" + id;
        }

        User user = getUser(principal);
        Article article = articleService.getById(id);

        Commentaire c = new Commentaire();
        c.setContenu(contenu);
        c.setUser(user);
        c.setArticle(article);
        c.setDateCommentaire(LocalDateTime.now());
        commentaireService.save(c);

        return "redirect:/articles/" + id;
    }

    // ─── DELETE COMMENTAIRE ─────────────────────────────────────────────────
    @PostMapping("/commentaires/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam Long articleId,
                                Principal principal) {
        User user = getUser(principal);
        commentaireService.delete(commentId, user);
        return "redirect:/articles/" + articleId;
    }

    @PostMapping("/devenir-auteur")
    public String devenirAuteur(Principal principal) {
        User user = getUser(principal);
        if (user.getRole() == com.blog.model.Role.LECTEUR) {
            return "redirect:/conditions?source=upgrade";
        }
        return "redirect:/articles/new";
    }

    @PostMapping("/devenir-auteur-confirm")
    public String devenirAuteurConfirm(Principal principal) {
        User user = getUser(principal);
        if (user.getRole() == com.blog.model.Role.LECTEUR) {
            user.setRole(com.blog.model.Role.AUTEUR);
            user.setConditionsAcceptees(true);
            userRepository.save(user);
        }
        return "redirect:/articles/new";
    }

    // ─── HELPER ─────────────────────────────────────────────────────────────
    private User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}
