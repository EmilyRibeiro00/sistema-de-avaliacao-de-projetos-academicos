package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "projeto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_projeto_sigaa_id_edicao", columnNames = {"sigaa_id", "edicao_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    private String sigaaId;

    private String titulo;

    private String conteudoTecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comite_id")
    private Comite comite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @Enumerated(EnumType.STRING)
    private ProjetoStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importado_de")
    private ImportacaoLote importadoDe;

    // Campus de origem do projeto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    private Boolean ativo = true;
}
