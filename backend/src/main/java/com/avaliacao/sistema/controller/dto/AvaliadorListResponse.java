package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.Usuario;

public record AvaliadorListResponse(
    Long id, String nome, String email, String cpf, String matricula, String siape,
    String instituicaoNome, String comiteNome, String statusAtivacao
) {

    public static AvaliadorListResponse de(Usuario u) {
        var pessoa = u.getPessoa();
        return new AvaliadorListResponse(u.getId(), pessoa.getNome(), pessoa.getEmail(), pessoa.getCpf(),
            pessoa.getMatricula(), pessoa.getSiape(),
            pessoa.getInstituicao() == null ? null : pessoa.getInstituicao().getNome(),
            u.getComite() == null ? null : u.getComite().getNome(),
            u.getStatusAtivacao().name());
    }
}
