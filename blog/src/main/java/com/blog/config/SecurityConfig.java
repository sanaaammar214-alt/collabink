package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/assets/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/uploads/**").permitAll()
                        .requestMatchers("/", "/register", "/login", "/about", "/contact", "/conditions").permitAll()
                        .requestMatchers("/articles", "/articles/{id}").permitAll()
                        .requestMatchers("/articles/search").permitAll()
                        .requestMatchers("/articles/devenir-auteur", "/articles/devenir-auteur-confirm").authenticated()
                        .requestMatchers("/articles/mes-articles").hasAnyRole("AUTEUR", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/articles/new", "/articles/save").hasAnyRole("AUTEUR", "ADMIN")
                        .requestMatchers("/articles/{id}/edit", "/articles/{id}/update").hasAnyRole("AUTEUR", "ADMIN")
                        .requestMatchers("/articles/{id}/delete").hasAnyRole("AUTEUR", "ADMIN")
                        .requestMatchers("/articles/{id}/like", "/articles/{id}/comments").authenticated()
                        .requestMatchers("/commentaires/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/403")
                )
                // CSRF Protection : Activé par défaut pour toutes les routes web (forms POST)
                // Empêche les attaques CSRF en exigeant un token valide pour chaque requête POST
                // Les templates Thymeleaf incluent automatiquement le token CSRF dans les formulaires
                .csrf(csrf -> csrf
                        // Ignorer CSRF pour les futures routes d'API (/api/**)
                        // Permettra les requêtes API sans token CSRF (ex: mobile apps, Postman)
                        .ignoringRequestMatchers("/api/**")
                );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String absolutePath = Paths.get(System.getProperty("user.dir"), uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .toString();
                String location = "file:" + absolutePath + File.separator;

                registry.addResourceHandler("/uploads/images/**")
                        .addResourceLocations(location);
            }
        };
    }
}