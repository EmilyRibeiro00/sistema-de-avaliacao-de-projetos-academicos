package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.controller.dto.LoginResponse;
import com.avaliacao.sistema.controller.dto.TrocarSenhaRequest;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conta")
@RequiredArgsConstructor
public class ContaController {

    private final AuthService authService;

    @PutMapping("/senha")
    public LoginResponse trocarSenha(@Valid @RequestBody TrocarSenhaRequest request,
                                      @AuthenticationPrincipal UsuarioPrincipal principal) {
        return authService.trocarSenha(principal.getId(), request.senhaAtual(), request.novaSenha());
    }
}
