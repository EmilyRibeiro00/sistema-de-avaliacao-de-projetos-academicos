package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

// Monta um usuário de mentira e injeta ele como o autenticado da requisição, sem passar pelo JwtAuthenticationFilter
final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    static Usuario usuario(long id, UsuarioTipo tipo) {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(id);
        pessoa.setNome("Usuario " + id);
        pessoa.setEmail("usuario" + id + "@teste.br");

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setPessoa(pessoa);
        usuario.setTipo(tipo);
        usuario.setStatusAtivacao(UsuarioStatusAtivacao.ATIVO);
        return usuario;
    }

    static RequestPostProcessor como(Usuario usuario) {
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        return authentication(auth);
    }
}
