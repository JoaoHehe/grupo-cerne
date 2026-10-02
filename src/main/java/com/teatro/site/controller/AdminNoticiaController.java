package com.teatro.site.controller;

import com.teatro.site.Noticia;
import com.teatro.site.ImagemNoticia;
import com.teatro.site.repository.NoticiaRepository;
import com.teatro.site.repository.UsuarioRepository;
import com.teatro.site.service.ArmazenamentoImagem;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/noticias")
public class AdminNoticiaController {

    private final NoticiaRepository noticiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArmazenamentoImagem armazenamento;

    public AdminNoticiaController(NoticiaRepository noticiaRepository,
                                  UsuarioRepository usuarioRepository,
                                  ArmazenamentoImagem armazenamento) {
        this.noticiaRepository = noticiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.armazenamento = armazenamento;
    }

    // Lista todas as notícias no painel
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("noticias", noticiaRepository.findAllByOrderByDataPublicacaoDesc());
        return "admin/noticias";
    }

    // Abre o formulário vazio
    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("noticia", new Noticia());
        return "admin/noticia-form";
    }

    // Salva uma notícia nova (com várias imagens e vídeo opcionais)
    @PostMapping
    public String salvar(@Valid @ModelAttribute("noticia") Noticia noticia,
                         BindingResult result,
                         @RequestParam("novasImagens") List<MultipartFile> novasImagens,
                         @RequestParam("novoVideo") MultipartFile novoVideo,
                         Principal principal) {

        // Garante que nenhuma mídia entre "de fora" do formulário
        noticia.setImagens(new ArrayList<>());
        noticia.setVideo(null);

        // Confere todos os arquivos ANTES de salvar qualquer coisa
        if (!result.hasErrors()) {
            try {
                validarMidias(novasImagens, novoVideo);
            } catch (IllegalArgumentException e) {
                result.reject("midia.invalida", e.getMessage());
            }
        }

        if (result.hasErrors()) {
            return "admin/noticia-form";
        }

        noticia.setId(null);
        noticia.setAutor(usuarioRepository.findByEmail(principal.getName()).orElseThrow());
        adicionarMidias(noticia, novasImagens, novoVideo);
        noticiaRepository.save(noticia);
        return "redirect:/admin/noticias";
    }

    // Abre o formulário preenchido para editar
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("noticia", noticiaRepository.findById(id).orElseThrow());
        return "admin/noticia-form";
    }

    // Atualiza título, conteúdo e mídias
    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
                            @Valid @ModelAttribute("noticia") Noticia form,
                            BindingResult result,
                            @RequestParam("novasImagens") List<MultipartFile> novasImagens,
                            @RequestParam("novoVideo") MultipartFile novoVideo,
                            @RequestParam(value = "removerImagens", required = false) List<Long> removerImagens,
                            @RequestParam(value = "removerVideo", required = false) boolean removerVideo) {

        Noticia existente = noticiaRepository.findById(id).orElseThrow();

        if (!result.hasErrors()) {
            try {
                validarMidias(novasImagens, novoVideo);
            } catch (IllegalArgumentException e) {
                result.reject("midia.invalida", e.getMessage());
            }
        }

        if (result.hasErrors()) {
            // O formulário continua mostrando as mídias atuais
            form.setId(id);
            form.setImagens(existente.getImagens());
            form.setVideo(existente.getVideo());
            return "admin/noticia-form";
        }

        existente.setTitulo(form.getTitulo());
        existente.setConteudo(form.getConteudo());

        // Remove as imagens marcadas (o arquivo e o registro)
        if (removerImagens != null) {
            for (ImagemNoticia img : new ArrayList<>(existente.getImagens())) {
                if (removerImagens.contains(img.getId())) {
                    armazenamento.apagar(img.getArquivo());
                    existente.getImagens().remove(img); // orphanRemoval apaga o registro do banco
                }
            }
        }

        // Vídeo: se veio um novo ou foi marcado pra remover, o antigo sai
        if (!novoVideo.isEmpty() || removerVideo) {
            armazenamento.apagar(existente.getVideo());
            existente.setVideo(null);
        }

        adicionarMidias(existente, novasImagens, novoVideo);
        noticiaRepository.save(existente);
        return "redirect:/admin/noticias";
    }

    // Apaga a notícia e todos os arquivos dela
    @PostMapping("/{id}/apagar")
    public String apagar(@PathVariable Long id) {
        noticiaRepository.findById(id).ifPresent(n -> {
            for (ImagemNoticia img : n.getImagens()) {
                armazenamento.apagar(img.getArquivo());
            }
            armazenamento.apagar(n.getVideo());
        });
        noticiaRepository.deleteById(id);
        return "redirect:/admin/noticias";
    }

    // Lança erro se algum arquivo enviado for inválido (não salva nada)
    private void validarMidias(List<MultipartFile> imagens, MultipartFile video) {
        for (MultipartFile f : imagens) {
            if (!f.isEmpty()) {
                armazenamento.validarImagem(f);
            }
        }

        if (!video.isEmpty()) {
            armazenamento.validarVideo(video);
        }
    }

    // Salva os arquivos e liga eles na notícia
    private void adicionarMidias(Noticia noticia, List<MultipartFile> imagens, MultipartFile video) {
        for (MultipartFile f : imagens) {
            if (!f.isEmpty()) {
                ImagemNoticia img = new ImagemNoticia();
                img.setArquivo(armazenamento.salvarImagem(f));
                img.setNoticia(noticia);
                noticia.getImagens().add(img);
            }
        }

        if (!video.isEmpty()) {
            noticia.setVideo(armazenamento.salvarVideo(video));
        }
    }
}