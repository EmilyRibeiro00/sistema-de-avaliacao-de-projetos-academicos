package com.avaliacao.sistema.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "integracao_credencial")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class IntegracaoCredencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    // Hash da API key — o valor em texto puro só existe no momento da geração, nunca é persistido
    private String chaveHash;

    private boolean ativo = true;

    private Instant criadoEm;

    private Instant ultimoUsoEm;
}
