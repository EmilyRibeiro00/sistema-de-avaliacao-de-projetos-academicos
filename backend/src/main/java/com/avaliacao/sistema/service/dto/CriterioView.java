package com.avaliacao.sistema.service.dto;

import java.math.BigDecimal;

public record CriterioView(Long id, String nome, BigDecimal peso, boolean ativo) {
}
