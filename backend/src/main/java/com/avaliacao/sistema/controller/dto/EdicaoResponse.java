package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.Edicao;
import java.math.BigDecimal;
import java.time.Instant;

public record EdicaoResponse(
    Long id,
    String nome,
    Integer ano,
    String status,
    Instant dataAbertura,
    Instant dataFechamento,
    Integer minAvaliadoresPorProjeto,
    Integer limiteMaxProjetosPorAvaliador,
    BigDecimal limiteDivergenciaNotas,
    BigDecimal escalaNotaMin,
    BigDecimal escalaNotaMax,
    boolean ativo
) {

    public static EdicaoResponse de(Edicao e) {
        return new EdicaoResponse(e.getId(), e.getNome(), e.getAno(), e.getStatus().name(), e.getDataAbertura(),
            e.getDataFechamento(), e.getMinAvaliadoresPorProjeto(), e.getLimiteMaxProjetosPorAvaliador(),
            e.getLimiteDivergenciaNotas(), e.getEscalaNotaMin(), e.getEscalaNotaMax(), e.getAtivo());
    }
}
