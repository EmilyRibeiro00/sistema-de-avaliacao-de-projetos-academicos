package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.ImportacaoLote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportacaoLoteRepository extends JpaRepository<ImportacaoLote, Long> {

    List<ImportacaoLote> findByEdicaoIdOrderByIniciadoEmDesc(Long edicaoId);
}
