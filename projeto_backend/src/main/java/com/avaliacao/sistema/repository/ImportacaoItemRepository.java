package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.ImportacaoItem;
import com.avaliacao.sistema.entity.enums.StatusImportacaoItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportacaoItemRepository extends JpaRepository<ImportacaoItem, Long> {

    List<ImportacaoItem> findByLoteId(Long loteId);

    List<ImportacaoItem> findByLoteIdAndStatus(Long loteId, StatusImportacaoItem status);
}
