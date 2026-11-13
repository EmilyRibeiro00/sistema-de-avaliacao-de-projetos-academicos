package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtribuicaoRepository extends JpaRepository<Atribuicao, Long> {

    List<Atribuicao> findByAvaliadorIdAndStatus(Long avaliadorId, AtribuicaoStatus status);

    List<Atribuicao> findByProjetoIdAndStatus(Long projetoId, AtribuicaoStatus status);

    Optional<Atribuicao> findByProjetoIdAndAvaliadorIdAndStatus(Long projetoId, Long avaliadorId, AtribuicaoStatus status);

    int countByAvaliadorIdAndStatus(Long avaliadorId, AtribuicaoStatus status);

    int countByAvaliadorIdAndStatusAndProjetoEdicaoId(Long avaliadorId, AtribuicaoStatus status, Long edicaoId);
}
