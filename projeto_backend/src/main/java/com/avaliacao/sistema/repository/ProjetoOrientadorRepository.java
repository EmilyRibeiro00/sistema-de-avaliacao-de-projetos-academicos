package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.ProjetoOrientador;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetoOrientadorRepository extends JpaRepository<ProjetoOrientador, Long> {

    List<ProjetoOrientador> findByProjetoId(Long projetoId);

    boolean existsByProjetoIdAndPessoaId(Long projetoId, Long pessoaId);
}
