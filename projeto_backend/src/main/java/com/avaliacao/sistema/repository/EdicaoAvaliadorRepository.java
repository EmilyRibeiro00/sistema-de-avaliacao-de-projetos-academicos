package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.enums.StatusParticipacaoAvaliador;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EdicaoAvaliadorRepository extends JpaRepository<EdicaoAvaliador, Long> {

    List<EdicaoAvaliador> findByEdicaoIdAndStatus(Long edicaoId, StatusParticipacaoAvaliador status);

    Optional<EdicaoAvaliador> findByEdicaoIdAndAvaliadorId(Long edicaoId, Long avaliadorId);

    int countByEdicaoIdAndAvaliadorId(Long edicaoId, Long avaliadorId);
}
