package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Projeto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    Optional<Projeto> findBySigaaIdAndEdicaoId(String sigaaId, Long edicaoId);

    // JOIN FETCH de propósito: comite/campus/instituicao são LAZY, e open-in-view=false — sem isso,
    // o controller (que já roda fora da transação) derruba com LazyInitializationException ao montar
    // o DTO de listagem.
    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.comite LEFT JOIN FETCH p.campus LEFT JOIN FETCH p.instituicao WHERE p.ativo = true")
    List<Projeto> findByAtivoTrue();

    @Query("SELECT p FROM Projeto p LEFT JOIN FETCH p.comite LEFT JOIN FETCH p.campus LEFT JOIN FETCH p.instituicao WHERE p.edicao.id = :edicaoId AND p.ativo = true")
    List<Projeto> findByEdicaoIdAndAtivoTrue(@Param("edicaoId") Long edicaoId);
}
