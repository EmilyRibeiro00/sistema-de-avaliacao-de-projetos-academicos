package com.avaliacao.sistema.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "criterio")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Criterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    private String nome;

    private BigDecimal peso;

    private Integer ordem;

    private Boolean ativo = true;
}
