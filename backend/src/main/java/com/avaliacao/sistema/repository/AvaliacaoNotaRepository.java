package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.AvaliacaoNota;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoNotaRepository extends JpaRepository<AvaliacaoNota, Long> {

    List<AvaliacaoNota> findByAvaliacaoId(Long avaliacaoId);
}
