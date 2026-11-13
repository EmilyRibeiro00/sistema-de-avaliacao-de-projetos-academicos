package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Campus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusRepository extends JpaRepository<Campus, Long> {

    Optional<Campus> findByNomeIgnoreCase(String nome);
}
