package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
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
import java.math.BigDecimal;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atribuicao_id", unique = true, nullable = false)
    private Atribuicao atribuicao;

    @Enumerated(EnumType.STRING)
    private AvaliacaoStatus status;

    // Calculada pelo servidor a partir de AvaliacaoNota x Criterio.peso — nunca aceita como input direto
    private BigDecimal notaFinal;

    private Instant dataEnvio;

    private Instant reabertaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reaberta_por")
    private Usuario reabertaPor;

    private String motivoReabertura;
}
