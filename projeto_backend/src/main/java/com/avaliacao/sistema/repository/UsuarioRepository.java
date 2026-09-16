package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByPessoaCpf(String cpf);

    Optional<Usuario> findByPessoaEmail(String email);

    Optional<Usuario> findByTokenAtivacao(String tokenAtivacao);
}
