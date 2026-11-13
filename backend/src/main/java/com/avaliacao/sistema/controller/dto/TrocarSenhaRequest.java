package com.avaliacao.sistema.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(
    @NotBlank String senhaAtual,
    @NotBlank @Size(min = 8) String novaSenha
) {
}
