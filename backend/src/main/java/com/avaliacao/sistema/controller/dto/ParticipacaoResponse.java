package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.EdicaoAvaliador;

public record ParticipacaoResponse(Long avaliadorId, Long edicaoId, String status, String comiteNome, String campusNome) {

    public static ParticipacaoResponse de(EdicaoAvaliador ea) {
        var comite = ea.getAvaliador().getComite();
        var campus = ea.getCampus();
        return new ParticipacaoResponse(ea.getAvaliador().getId(), ea.getEdicao().getId(), ea.getStatus().name(),
            comite == null ? null : comite.getNome(), campus == null ? null : campus.getNome());
    }
}
