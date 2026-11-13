package com.avaliacao.sistema.security;

import com.avaliacao.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // "username" aqui é o identificador de login: e-mail ou CPF (regra 1: login por e-mail/CPF + senha)
    @Override
    public UserDetails loadUserByUsername(String identificador) {
        return usuarioRepository.findByPessoaEmail(identificador)
            .or(() -> usuarioRepository.findByPessoaCpf(identificador))
            .map(UsuarioPrincipal::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + identificador));
    }
}
