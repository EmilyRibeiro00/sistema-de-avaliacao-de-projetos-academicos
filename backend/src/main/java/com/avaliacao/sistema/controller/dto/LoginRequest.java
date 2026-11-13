package com.avaliacao.sistema.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String identificador,
    @NotBlank String senha
) {
}
