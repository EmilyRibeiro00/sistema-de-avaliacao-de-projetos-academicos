package com.avaliacao.sistema.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

// chave = id do critério, valor = nota
public record NotasRequest(@NotNull Map<Long, BigDecimal> notas) {
}
