package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.enums.StatusEdicao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdicaoRepository extends JpaRepository<Edicao, Long> {

    Optional<Edicao> findByStatus(StatusEdicao status);
}
