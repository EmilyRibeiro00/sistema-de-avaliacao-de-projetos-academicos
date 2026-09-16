package com.avaliacao.sistema.web.dto;

public record LoginResponse(
    String token,
    String tipo
) {
}
