package com.avaliacao.sistema.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MotivoRequest(@NotBlank String motivo) {
}
