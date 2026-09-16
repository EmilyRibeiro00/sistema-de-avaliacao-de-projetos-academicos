package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.AreaConhecimentoAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaConhecimentoAliasRepository extends JpaRepository<AreaConhecimentoAlias, Long> {

    Optional<AreaConhecimentoAlias> findByTextoOrigem(String textoOrigem);

    List<AreaConhecimentoAlias> findByAreaIsNull();
}
