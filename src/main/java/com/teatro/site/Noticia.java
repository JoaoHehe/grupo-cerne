package com.teatro.site;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Noticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "O conteúdo é obrigatório")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    private LocalDateTime dataPublicacao = LocalDateTime.now();

    // Várias imagens por notícia. Ao apagar a notícia (ou tirar da lista), o registro some junto
    @OneToMany(mappedBy = "noticia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ImagemNoticia> imagens = new ArrayList<>();

    // Nome do arquivo do vídeo. Vazio = sem vídeo
    private String video;

    // Quem postou a notícia
    @ManyToOne
    private Usuario autor;
}