package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.StatusAtivacao;
import com.avaliacao.sistema.entity.enums.TipoUsuario;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", unique = true, nullable = false)
    private Pessoa pessoa;

    private String senhaHash;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    @Enumerated(EnumType.STRING)
    private StatusAtivacao statusAtivacao;

    private String tokenAtivacao;

    private Instant tokenAtivacaoExpiraEm;

    private Instant createdAt;

    private Instant updatedAt;
}
