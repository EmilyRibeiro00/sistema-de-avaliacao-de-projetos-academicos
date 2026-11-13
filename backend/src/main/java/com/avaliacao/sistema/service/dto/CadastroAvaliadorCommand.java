package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CadastroAvaliadorCommand(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank String cpf,
    String matricula,
    String siape,
    String instituicaoNome
) {
}
