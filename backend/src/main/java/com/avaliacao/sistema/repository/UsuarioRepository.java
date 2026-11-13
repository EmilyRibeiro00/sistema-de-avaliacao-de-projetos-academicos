package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByPessoaCpf(String cpf);

    Optional<Usuario> findByPessoaEmail(String email);

    // JOIN FETCH de propósito: comite e pessoa.instituicao são LAZY, e open-in-view=false — sem isso,
    // o controller (que já roda fora da transação) derruba com LazyInitializationException ao montar
    // o DTO de listagem.
    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.instituicao LEFT JOIN FETCH u.comite WHERE u.tipo = :tipo")
    List<Usuario> findByTipo(@Param("tipo") UsuarioTipo tipo);

    // Mesmo motivo do findByTipo acima: usado em editar/remover avaliador, cuja resposta é serializada
    // fora da transação.
    @Query("SELECT u FROM Usuario u JOIN FETCH u.pessoa p LEFT JOIN FETCH p.instituicao LEFT JOIN FETCH u.comite WHERE u.id = :id")
    Optional<Usuario> findByIdComDetalhes(@Param("id") Long id);
}
