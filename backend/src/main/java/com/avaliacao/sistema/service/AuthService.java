package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.security.JwtService;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.controller.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(String identificador, String senha) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(identificador, senha)
        );
        UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
        String token = jwtService.gerarToken(principal);
        return new LoginResponse(token, principal.getUsuario().getTipo().name());
    }

    // Retorna um token novo (em vez de 204) pra o front substituir a sessão em memória: o JWT antigo
    // ainda traria precisaTrocarSenha=true, já que claims são uma foto do momento do login.
    @Transactional
    public LoginResponse trocarSenha(Long usuarioId, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário", usuarioId));

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraNegocioException("Senha atual incorreta");
        }

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setPrecisaTrocarSenha(false);
        usuarioRepository.save(usuario);

        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        String token = jwtService.gerarToken(principal);
        return new LoginResponse(token, usuario.getTipo().name());
    }
}
