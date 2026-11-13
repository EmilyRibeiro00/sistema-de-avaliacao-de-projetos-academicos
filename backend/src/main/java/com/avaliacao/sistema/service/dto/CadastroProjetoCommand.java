package com.avaliacao.sistema.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// sigaaId é opcional de propósito: isto é cadastro MANUAL (sem vir de importação real do Sigaa).
// Se não vier, o serviço gera um identificador próprio. comiteNome/campusNome também são opcionais
// aqui (projeto.comite_id/campus_id não têm NOT NULL no banco) — podem ficar pendentes de
// complementação pelo admin, igual já acontecia com área antes.
public record CadastroProjetoCommand(
    @NotNull Long edicaoId,
    String sigaaId,
    @NotBlank String titulo,
    @NotBlank String conteudoTecnico,
    String comiteNome,
    String campusNome,
    String instituicaoNome,
    @NotBlank String autorNome,
    String autorEmail,
    String autorCpf
) {
}
