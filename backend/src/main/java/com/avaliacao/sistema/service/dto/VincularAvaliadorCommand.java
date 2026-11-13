package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VincularAvaliadorCommand(
    @NotNull Long edicaoId,
    @NotBlank String comiteNome,
    @NotBlank String campusNome,
    Integer limiteMaxProjetos
) {
}
