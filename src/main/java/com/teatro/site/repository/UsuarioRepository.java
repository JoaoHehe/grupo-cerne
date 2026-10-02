package com.teatro.site.repository;

import com.teatro.site.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Usado pelo login para achar o usuário pelo email
    Optional<Usuario> findByEmail(String email);
}