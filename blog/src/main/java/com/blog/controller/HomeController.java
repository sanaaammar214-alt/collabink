package com.blog.controller;

import com.blog.model.User;
import com.blog.repository.CategorieRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ArticleService articleService;
    private final CategorieRepository categorieRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        model.addAttribute("articles", articleService.getAll().stream().limit(6).toList());
        model.addAttribute("topArticles", articleService.getAllOrderByLikes().stream().limit(3).toList());
        model.addAttribute("categories", categorieRepository.findAll());

        if (principal != null) {
            userRepository.findByEmailWithLikes(principal.getName()).ifPresent(user -> {
                model.addAttribute("recommendedArticles", articleService.getRecommendations(user));
            });
        }
        
        return "public/index";
    }

    @GetMapping("/auteur/{id}")
    public String auteur(@PathVariable Long id, Model model) {
        User auteur = userService.getById(id);
        var articles = articleService.getByAuteur(id);
        
        long totalLikes = articles.stream()
                .mapToLong(article -> article.getLikes().size())
                .sum();
        
        model.addAttribute("auteur", auteur);
        model.addAttribute("articles", articles);
        model.addAttribute("totalLikes", totalLikes);
        
        return "public/profil-auteur";
    }
}
