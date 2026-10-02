package com.teatro.site.controller;

import com.teatro.site.Noticia;
import com.teatro.site.repository.NoticiaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PaginaController {

    private final NoticiaRepository noticiaRepository;

    public PaginaController(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("noticias", noticiaRepository.findAllByOrderByDataPublicacaoDesc());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Página de uma notícia, aberta para qualquer pessoa
    @GetMapping("/noticia/{id}")
    public String noticia(@PathVariable Long id, Model model) {
        Noticia noticia = noticiaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("noticia", noticia);

        // Barra lateral: 5 mais recentes, sem a que está aberta
        model.addAttribute("recentes", noticiaRepository.findTop5ByIdNotOrderByDataPublicacaoDesc(id));
        return "noticia";
    }
}