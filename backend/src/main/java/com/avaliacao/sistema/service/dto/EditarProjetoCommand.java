package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.NotBlank;

// edicaoId, sigaaId e autor não são editáveis aqui de propósito: sigaaId+edicaoId formam a identidade
// do projeto (uk_projeto_sigaa_id_edicao) e autoria é outra relação (ProjetoDiscente), fora de escopo
// de uma edição simples de metadados.
public record EditarProjetoCommand(
    @NotBlank String titulo,
    @NotBlank String conteudoTecnico,
    String comiteNome,
    String campusNome,
    String instituicaoNome
) {
}
