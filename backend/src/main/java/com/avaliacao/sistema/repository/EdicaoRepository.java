package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdicaoRepository extends JpaRepository<Edicao, Long> {

    Optional<Edicao> findByStatus(EdicaoStatus status);

    List<Edicao> findByAtivoTrue();
}
