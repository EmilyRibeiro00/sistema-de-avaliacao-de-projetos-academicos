package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.ProjetoDiscente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoDiscenteRepository extends JpaRepository<ProjetoDiscente, Long> {

    List<ProjetoDiscente> findByProjetoId(Long projetoId);

    boolean existsByProjetoIdAndPessoaId(Long projetoId, Long pessoaId);
}
