package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.enums.StatusAtribuicao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtribuicaoRepository extends JpaRepository<Atribuicao, Long> {

    List<Atribuicao> findByAvaliadorIdAndStatus(Long avaliadorId, StatusAtribuicao status);

    List<Atribuicao> findByProjetoIdAndStatus(Long projetoId, StatusAtribuicao status);

    Optional<Atribuicao> findByProjetoIdAndAvaliadorIdAndStatus(Long projetoId, Long avaliadorId, StatusAtribuicao status);

    int countByAvaliadorIdAndStatus(Long avaliadorId, StatusAtribuicao status);
}
