package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Avaliacao;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByAtribuicaoId(Long atribuicaoId);
}
