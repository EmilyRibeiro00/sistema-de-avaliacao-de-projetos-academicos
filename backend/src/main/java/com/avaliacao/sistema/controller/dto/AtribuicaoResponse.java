package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.Atribuicao;

public record AtribuicaoResponse(Long id, Long projetoId, Long avaliadorId, String status, String origem) {

    public static AtribuicaoResponse de(Atribuicao a) {
        return new AtribuicaoResponse(a.getId(), a.getProjeto().getId(), a.getAvaliador().getId(),
            a.getStatus().name(), a.getOrigem().name());
    }
}
