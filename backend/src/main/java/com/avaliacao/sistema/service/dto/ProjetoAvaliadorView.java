package com.avaliacao.sistema.service.dto;

// Visão cega: só título, comitê e conteúdo técnico. Nunca autor, orientador ou instituição.
public record ProjetoAvaliadorView(
    Long atribuicaoId,
    Long projetoId,
    String titulo,
    String comite,
    String conteudoTecnico,
    String statusAvaliacao
) {
}
