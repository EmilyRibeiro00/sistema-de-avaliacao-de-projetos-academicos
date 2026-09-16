package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.StatusAtivacao;
import com.avaliacao.sistema.exception.TokenInvalidoException;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.security.JwtService;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.web.dto.LoginResponse;
import java.time.Instant;
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

    @Transactional
    public void ativarConta(String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByTokenAtivacao(token)
            .filter(u -> u.getTokenAtivacaoExpiraEm() != null && u.getTokenAtivacaoExpiraEm().isAfter(Instant.now()))
            .orElseThrow(() -> new TokenInvalidoException("Token de ativação inválido ou expirado"));

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setStatusAtivacao(StatusAtivacao.ATIVO);
        usuario.setTokenAtivacao(null);
        usuario.setTokenAtivacaoExpiraEm(null);
    }
}
