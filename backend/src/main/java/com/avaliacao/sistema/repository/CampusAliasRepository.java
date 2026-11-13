package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.CampusAlias;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusAliasRepository extends JpaRepository<CampusAlias, Long> {

    Optional<CampusAlias> findByTextoOrigem(String textoOrigem);

    Optional<CampusAlias> findByTextoOrigemIgnoreCase(String textoOrigem);

    List<CampusAlias> findByCampusIsNull();
}
