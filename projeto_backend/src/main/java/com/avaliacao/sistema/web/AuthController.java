package com.avaliacao.sistema.web;

import com.avaliacao.sistema.service.AuthService;
import com.avaliacao.sistema.web.dto.AtivarContaRequest;
import com.avaliacao.sistema.web.dto.LoginRequest;
import com.avaliacao.sistema.web.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.identificador(), request.senha());
    }

    @PostMapping("/ativar")
    public ResponseEntity<Void> ativarConta(@Valid @RequestBody AtivarContaRequest request) {
        authService.ativarConta(request.token(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
