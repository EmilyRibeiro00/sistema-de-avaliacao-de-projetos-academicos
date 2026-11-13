package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Comite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComiteRepository extends JpaRepository<Comite, Long> {

    Optional<Comite> findByNomeIgnoreCase(String nome);
}
