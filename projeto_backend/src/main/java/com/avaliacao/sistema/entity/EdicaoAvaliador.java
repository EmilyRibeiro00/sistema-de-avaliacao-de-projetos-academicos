package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.StatusParticipacaoAvaliador;
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
    name = "edicao_avaliador",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_edicao_avaliador", columnNames = {"edicao_id", "avaliador_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class EdicaoAvaliador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliador_id", nullable = false)
    private Usuario avaliador;

    @Enumerated(EnumType.STRING)
    private StatusParticipacaoAvaliador status;

    private Integer limiteMaxProjetos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importado_de")
    private ImportacaoLote importadoDe;
}
