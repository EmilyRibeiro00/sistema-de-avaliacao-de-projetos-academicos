package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public record EdicaoCommand(
    @NotBlank String nome,
    @NotNull Integer ano,
    @NotNull Instant dataAbertura,
    @NotNull Instant dataFechamento,
    Integer minAvaliadoresPorProjeto,
    Integer limiteMaxProjetosPorAvaliador,
    BigDecimal limiteDivergenciaNotas,
    BigDecimal escalaNotaMin,
    BigDecimal escalaNotaMax
) {
}
