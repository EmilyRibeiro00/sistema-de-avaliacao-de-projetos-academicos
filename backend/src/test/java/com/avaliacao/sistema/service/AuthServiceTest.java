package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.security.JwtService;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.controller.dto.LoginResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(authenticationManager, usuarioRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginDevolveOTokenEOTipoDoUsuario() {
        Usuario admin = TestData.avaliador(1);
        admin.setTipo(UsuarioTipo.ADMIN);
        UsuarioPrincipal principal = new UsuarioPrincipal(admin);
        when(authenticationManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(jwtService.gerarToken(principal)).thenReturn("jwt-abc");

        LoginResponse resposta = service.login("pessoa1@teste.br", "senha");

        assertThat(resposta.token()).isEqualTo("jwt-abc");
        assertThat(resposta.tipo()).isEqualTo("ADMIN");
    }

    @Test
    void loginComCredenciaisInvalidasPropagaAExcecaoDeAutenticacao() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("ruim"));

        assertThatThrownBy(() -> service.login("x@teste.br", "errada")).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void trocarSenhaAtualizaHashLimpaFlagEDevolveNovoToken() {
        Usuario usuario = TestData.avaliador(5);
        usuario.setSenhaHash("hash-antigo");
        usuario.setPrecisaTrocarSenha(true);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaAtual", "hash-antigo")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("hash-novo");
        when(jwtService.gerarToken(any())).thenReturn("jwt-novo");

        LoginResponse resposta = service.trocarSenha(5L, "senhaAtual", "novaSenha123");

        assertThat(usuario.getSenhaHash()).isEqualTo("hash-novo");
        assertThat(usuario.getPrecisaTrocarSenha()).isFalse();
        assertThat(resposta.token()).isEqualTo("jwt-novo");
    }

    @Test
    void trocarSenhaComSenhaAtualIncorretaLancaRegraDeNegocioENaoAltera() {
        Usuario usuario = TestData.avaliador(5);
        usuario.setSenhaHash("hash-antigo");
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "hash-antigo")).thenReturn(false);

        assertThatThrownBy(() -> service.trocarSenha(5L, "senhaErrada", "novaSenha123"))
            .isInstanceOf(RegraNegocioException.class);
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-antigo");
    }

    @Test
    void trocarSenhaDeUsuarioInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trocarSenha(99L, "qualquer", "novaSenha123"))
            .isInstanceOf(com.avaliacao.sistema.exception.EntidadeNaoEncontradaException.class);
    }
}
