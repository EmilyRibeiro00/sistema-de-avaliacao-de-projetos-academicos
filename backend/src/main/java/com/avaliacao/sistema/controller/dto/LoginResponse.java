package com.avaliacao.sistema.controller.dto;

public record LoginResponse(
    String token,
    String tipo
) {
}
