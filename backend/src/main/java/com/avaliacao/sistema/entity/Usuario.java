package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER: LAZY causava LazyInitializationException — o fluxo de login lê Pessoa fora da transação original
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pessoa_id", unique = true, nullable = false)
    private Pessoa pessoa;

    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private UsuarioTipo tipo;

    @Enumerated(EnumType.STRING)
    private UsuarioStatusAtivacao statusAtivacao;

    private Boolean precisaTrocarSenha = false;

    private Instant createdAt;

    private Instant updatedAt;

    // Comitê é fixo pro avaliador (não varia por edição, diferente de campus, que é por EdicaoAvaliador)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comite_id")
    private Comite comite;
}
