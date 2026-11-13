package com.avaliacao.sistema.service.dto;

import java.math.BigDecimal;

public record NotaAvaliadorView(Long avaliadorId, String avaliadorNome, BigDecimal notaFinal) {
}
