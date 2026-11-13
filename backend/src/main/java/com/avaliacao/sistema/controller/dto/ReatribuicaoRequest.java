package com.avaliacao.sistema.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReatribuicaoRequest(@NotNull Long novoAvaliadorId, @NotBlank String motivo) {
}
