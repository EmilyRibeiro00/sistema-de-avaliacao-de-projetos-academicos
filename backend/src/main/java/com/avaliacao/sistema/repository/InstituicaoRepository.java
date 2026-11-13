package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Instituicao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

    Optional<Instituicao> findByNomeIgnoreCase(String nome);
}
