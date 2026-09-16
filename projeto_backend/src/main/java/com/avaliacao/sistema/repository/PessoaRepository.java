package com.avaliacao.sistema.repository;

import com.avaliacao.sistema.entity.Pessoa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessoaRepository extends JpaRepository<Pessoa, Long> {

    Optional<Pessoa> findByCpf(String cpf);

    Optional<Pessoa> findBySiapeAndInstituicaoId(String siape, Long instituicaoId);

    Optional<Pessoa> findByMatriculaAndInstituicaoId(String matricula, Long instituicaoId);
}
