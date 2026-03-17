package com.blog.controller;

import com.blog.service.ArticleService;
import com.blog.service.CommentaireService;
import com.blog.service.FileUploadService;
import com.blog.service.LikeService;
import com.blog.repository.CategorieRepository;
import com.blog.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private LikeService likeService;

    @MockBean
    private CommentaireService commentaireService;

    @MockBean
    private FileUploadService fileUploadService;

    @MockBean
    private CategorieRepository categorieRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void liste_sansConnexion_doitRetourner200() throws Exception {
        // Mocker articleService.getAllPaginated(0,6) → Page.empty()
        when(articleService.getAllPaginated(0, 6)).thenReturn(Page.empty());
        
        // Mocker categorieRepository.findAll() → List.of()
        when(categorieRepository.findAll()).thenReturn(Collections.emptyList());

        // perform(get("/articles"))
        mockMvc.perform(get("/articles"))
                .andExpect(status().isOk())
                .andExpect(view().name("articles/liste"));
    }

    @Test
    void newForm_sansConnexion_doitRedirigerLogin() throws Exception {
        // perform(get("/articles/new"))
        mockMvc.perform(get("/articles/new"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "AUTEUR")
    void newForm_avecAuteur_doitRetourner200() throws Exception {
        // Mocker categorieRepository.findAll() → List.of()
        when(categorieRepository.findAll()).thenReturn(Collections.emptyList());

        // perform(get("/articles/new"))
        mockMvc.perform(get("/articles/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("articles/form"));
    }

    @Test
    @WithMockUser(roles = "LECTEUR")
    void adminDashboard_avecLecteur_doitRetourner403() throws Exception {
        // perform(get("/admin/dashboard"))
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
