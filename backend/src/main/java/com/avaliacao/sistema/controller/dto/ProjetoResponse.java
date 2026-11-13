package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.Projeto;

public record ProjetoResponse(
    Long id, Long edicaoId, String sigaaId, String titulo, String conteudoTecnico,
    String comite, String campus, String instituicao, String status, boolean ativo
) {

    public static ProjetoResponse de(Projeto p) {
        return new ProjetoResponse(p.getId(), p.getEdicao().getId(), p.getSigaaId(), p.getTitulo(), p.getConteudoTecnico(),
            p.getComite() == null ? null : p.getComite().getNome(),
            p.getCampus() == null ? null : p.getCampus().getNome(),
            p.getInstituicao() == null ? null : p.getInstituicao().getNome(),
            p.getStatus().name(), p.getAtivo());
    }
}
