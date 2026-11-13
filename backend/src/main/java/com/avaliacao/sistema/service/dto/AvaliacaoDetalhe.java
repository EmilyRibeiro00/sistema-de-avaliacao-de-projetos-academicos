package com.avaliacao.sistema.service.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AvaliacaoDetalhe(
    ProjetoAvaliadorView projeto,
    List<CriterioView> criterios,
    Map<Long, BigDecimal> notas,
    BigDecimal notaFinal,
    boolean editavel
) {
}
