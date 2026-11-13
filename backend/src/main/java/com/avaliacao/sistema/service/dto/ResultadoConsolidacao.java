package com.avaliacao.sistema.service.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResultadoConsolidacao(
    Long projetoId,
    int avaliadoresAtribuidos,
    int avaliacoesEnviadas,
    boolean avaliado,
    BigDecimal notaDoProjeto,
    BigDecimal divergencia,
    boolean divergenciaAcimaDoLimite,
    List<NotaAvaliadorView> notasPorAvaliador
) {
}
