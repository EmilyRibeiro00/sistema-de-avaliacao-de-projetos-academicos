package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "edicao")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Edicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer ano;

    @Enumerated(EnumType.STRING)
    private EdicaoStatus status;

    private Instant dataAbertura;

    private Instant dataFechamento;

    private Integer minAvaliadoresPorProjeto = 3;

    private Integer limiteMaxProjetosPorAvaliador;

    private BigDecimal limiteDivergenciaNotas;

    private BigDecimal escalaNotaMin = BigDecimal.ZERO;

    private BigDecimal escalaNotaMax = BigDecimal.TEN;

    private Boolean ativo = true;
}
