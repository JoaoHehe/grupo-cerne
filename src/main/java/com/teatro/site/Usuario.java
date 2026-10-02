package com.teatro.site;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // Email é o login, então não pode repetir
    @Column(unique = true, nullable = false)
    private String email;

    // Aqui vai a senha já criptografada, nunca a senha pura
    @Column(nullable = false)
    private String senha;
}