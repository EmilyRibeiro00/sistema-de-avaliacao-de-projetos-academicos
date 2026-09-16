package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Projeto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    Optional<Projeto> findBySigaaIdAndEdicaoId(String sigaaId, Long edicaoId);

    List<Projeto> findByEdicaoId(Long edicaoId);
}
