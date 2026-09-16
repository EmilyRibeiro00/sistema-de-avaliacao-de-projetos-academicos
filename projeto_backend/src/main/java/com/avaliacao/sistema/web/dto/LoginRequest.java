package com.avaliacao.sistema.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String identificador,
    @NotBlank String senha
) {
}
