package com.avaliacao.sistema.service.dto;

import java.util.List;

public record ResultadoAtribuicao(
    Long projetoId,
    int jaAtribuidos,
    int novosAtribuidos,
    int faltantes,
    List<Long> avaliadoresSorteados,
    List<String> avisos
) {

    public boolean completo() {
        return faltantes == 0;
    }
}
