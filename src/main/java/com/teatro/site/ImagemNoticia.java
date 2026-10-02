package com.teatro.site;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ImagemNoticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do arquivo dentro da pasta uploads
    @Column(nullable = false)
    private String arquivo;

    // A qual notícia essa imagem pertence
    @ManyToOne(optional = false)
    private Noticia noticia;
}