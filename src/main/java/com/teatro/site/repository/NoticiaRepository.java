package com.teatro.site.repository;

import com.teatro.site.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    // Lista as notícias da mais nova para a mais antiga
    List<Noticia> findAllByOrderByDataPublicacaoDesc();
}