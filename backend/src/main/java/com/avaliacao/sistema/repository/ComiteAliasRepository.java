package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.ComiteAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComiteAliasRepository extends JpaRepository<ComiteAlias, Long> {

    Optional<ComiteAlias> findByTextoOrigem(String textoOrigem);

    Optional<ComiteAlias> findByTextoOrigemIgnoreCase(String textoOrigem);

    List<ComiteAlias> findByComiteIsNull();
}
