package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Criterio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterioRepository extends JpaRepository<Criterio, Long> {

    List<Criterio> findByEdicaoIdAndAtivoTrueOrderByOrdem(Long edicaoId);
}
