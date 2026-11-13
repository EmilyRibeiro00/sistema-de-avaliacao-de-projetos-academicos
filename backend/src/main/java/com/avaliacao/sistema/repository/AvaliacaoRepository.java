package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Avaliacao;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByAtribuicaoId(Long atribuicaoId);

    List<Avaliacao> findByAtribuicaoIdIn(Collection<Long> atribuicaoIds);
}
