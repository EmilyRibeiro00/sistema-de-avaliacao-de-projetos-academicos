package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CriterioCommand(
    @NotBlank String nome,
    @NotNull BigDecimal peso,
    Integer ordem
) {
}
