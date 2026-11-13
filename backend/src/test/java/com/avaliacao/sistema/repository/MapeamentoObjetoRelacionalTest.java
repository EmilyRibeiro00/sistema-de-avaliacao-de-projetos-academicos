package com.avaliacao.sistema.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

// Checa constraints do schema que não dependem de índice parcial (H2 não suporta WHERE em índice único)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MapeamentoObjetoRelacionalTest {

    @Autowired PessoaRepository pessoaRepository;
    @Autowired EdicaoRepository edicaoRepository;
    @Autowired ProjetoRepository projetoRepository;

    private Pessoa pessoa(String cpf) {
        Pessoa p = new Pessoa();
        p.setNome("Pessoa " + cpf);
        p.setCpf(cpf);
        return pessoaRepository.saveAndFlush(p);
    }

    private Edicao edicao() {
        Edicao e = new Edicao();
        e.setNome("Edição de teste");
        e.setAno(2099);
        e.setStatus(EdicaoStatus.PLANEJADA);
        return edicaoRepository.saveAndFlush(e);
    }

    private Projeto projeto(Edicao edicao, String sigaaId) {
        Projeto p = new Projeto();
        p.setEdicao(edicao);
        p.setSigaaId(sigaaId);
        p.setTitulo("Projeto " + sigaaId);
        p.setStatus(ProjetoStatus.SUBMETIDO);
        return projetoRepository.saveAndFlush(p);
    }

    @Test
    void cpfDuplicadoViolaConstraintUnique() {
        pessoa("22222222201");

        assertThatThrownBy(() -> pessoa("22222222201")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void edicaoAplicaOsDefaultsDoSchema() {
        Edicao salva = edicao();

        Edicao lida = edicaoRepository.findById(salva.getId()).orElseThrow();
        assertThat(lida.getMinAvaliadoresPorProjeto()).isEqualTo(3);
        assertThat(lida.getEscalaNotaMin()).isEqualByComparingTo("0");
        assertThat(lida.getEscalaNotaMax()).isEqualByComparingTo("10");
    }

    @Test
    void sigaaIdEhUnicoPorEdicaoMasPodeRepetirEmOutraEdicao() {
        Edicao e1 = edicao();
        Edicao e2 = edicao();
        projeto(e1, "SIGAA-DUP");
        projeto(e2, "SIGAA-DUP");

        assertThat(projetoRepository.findBySigaaIdAndEdicaoId("SIGAA-DUP", e2.getId())).isPresent();
        assertThatThrownBy(() -> projeto(e1, "SIGAA-DUP")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByEdicaoIdTrazSoOsProjetosDaquelaEdicao() {
        Edicao e1 = edicao();
        Edicao e2 = edicao();
        projeto(e1, "SIGAA-A");
        projeto(e1, "SIGAA-B");
        projeto(e2, "SIGAA-C");

        assertThat(projetoRepository.findByEdicaoIdAndAtivoTrue(e1.getId())).hasSize(2);
        assertThat(projetoRepository.findByEdicaoIdAndAtivoTrue(e2.getId())).hasSize(1);
    }
}
