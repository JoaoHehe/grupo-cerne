package com.teatro.site.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ArmazenamentoImagem {

    // Tipos aceitos e a extensão de cada um. SVG fica de fora de propósito (pode carregar script)
    private static final Map<String, String> TIPOS_IMAGEM = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private static final Map<String, String> TIPOS_VIDEO = Map.of(
            "video/mp4", ".mp4",
            "video/webm", ".webm"
    );

    // Limite de cada imagem: 5MB
    private static final long TAMANHO_MAX_IMAGEM = 5L * 1024 * 1024;

    private final Path pasta;

    public ArmazenamentoImagem(@Value("${app.upload.dir}") String dir) throws IOException {
        this.pasta = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(pasta);
    }

    // Só confere se a imagem é válida, sem salvar nada. Lança erro se não for
    public void validarImagem(MultipartFile arquivo) {
        if (extensao(arquivo, TIPOS_IMAGEM) == null) {
            throw new IllegalArgumentException("Envie imagens JPG, PNG, WEBP ou GIF.");
        }
        if (arquivo.getSize() > TAMANHO_MAX_IMAGEM) {
            throw new IllegalArgumentException("Cada imagem pode ter no máximo 5MB.");
        }
    }

    public void validarVideo(MultipartFile arquivo) {
        if (extensao(arquivo, TIPOS_VIDEO) == null) {
            throw new IllegalArgumentException("Envie um vídeo MP4 ou WEBM.");
        }
    }

    // Salva a imagem e devolve o nome do arquivo
    public String salvarImagem(MultipartFile arquivo) {
        validarImagem(arquivo);
        return guardar(arquivo, extensao(arquivo, TIPOS_IMAGEM));
    }

    // Salva o vídeo e devolve o nome do arquivo
    public String salvarVideo(MultipartFile arquivo) {
        validarVideo(arquivo);
        return guardar(arquivo, extensao(arquivo, TIPOS_VIDEO));
    }

    // Apaga o arquivo. Se o nome for vazio ou o arquivo não existir, não faz nada
    public void apagar(String nome) {
        if (nome == null || nome.isBlank()) {
            return;
        }
        try {
            Path arquivo = pasta.resolve(nome).normalize();
            if (arquivo.startsWith(pasta)) { // só apaga dentro da pasta uploads
                Files.deleteIfExists(arquivo);
            }
        } catch (IOException e) {
            // Não travamos o sistema por causa de um arquivo que não deu pra apagar
        }
    }

    // Devolve a extensão do tipo do arquivo, ou null se o tipo não for aceito
    private String extensao(MultipartFile arquivo, Map<String, String> tipos) {
        String tipo = arquivo.getContentType();
        return (tipo == null) ? null : tipos.get(tipo);
    }

    // Grava o arquivo com nome aleatório (não usa o nome que veio do usuário)
    private String guardar(MultipartFile arquivo, String extensao) {
        String nome = UUID.randomUUID() + extensao;
        try (InputStream in = arquivo.getInputStream()) {
            Files.copy(in, pasta.resolve(nome));
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao salvar o arquivo", e);
        }
        return nome;
    }
}
