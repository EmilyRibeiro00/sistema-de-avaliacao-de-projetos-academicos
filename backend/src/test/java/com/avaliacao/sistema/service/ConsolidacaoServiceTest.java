package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Avaliacao;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.repository.AtribuicaoRepository;
import com.avaliacao.sistema.repository.AvaliacaoRepository;
import com.avaliacao.sistema.repository.ProjetoRepository;
import com.avaliacao.sistema.service.dto.ResultadoConsolidacao;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsolidacaoServiceTest {

    @Mock ProjetoRepository projetoRepository;
    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;

    ConsolidacaoService service;
    Edicao edicao;
    Projeto projeto;
    List<Atribuicao> atribuicoes;
    List<Avaliacao> avaliacoesExistentes;

    @BeforeEach
    void setUp() {
        service = new ConsolidacaoService(projetoRepository, atribuicaoRepository, avaliacaoRepository);
        edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        edicao.setLimiteDivergenciaNotas(new BigDecimal("2.5"));
        projeto = TestData.projeto(1L, edicao, TestData.comite(1L), TestData.campus(1L));
        atribuicoes = new ArrayList<>();
        avaliacoesExistentes = new ArrayList<>();

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(atribuicaoRepository.findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA)).thenReturn(atribuicoes);
        when(avaliacaoRepository.findByAtribuicaoIdIn(any())).thenAnswer(inv -> {
            Collection<Long> ids = inv.getArgument(0);
            return avaliacoesExistentes.stream().filter(a -> ids.contains(a.getAtribuicao().getId())).toList();
        });
    }

    /** Adiciona uma atribuição ativa; nota nula = sem avaliação ainda. */
    private void avaliador(long id, AvaliacaoStatus status, String nota) {
        Atribuicao atribuicao = TestData.atribuicao(id, projeto, TestData.avaliador(id), AtribuicaoStatus.ATIVA);
        atribuicoes.add(atribuicao);
        if (status == null) {
            return;
        }
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setAtribuicao(atribuicao);
        avaliacao.setStatus(status);
        avaliacao.setNotaFinal(nota == null ? null : new BigDecimal(nota));
        avaliacoesExistentes.add(avaliacao);
    }

    @Test
    void notaDoProjetoEhAMediaDasNotasFinaisEnviadas() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.50");
        avaliador(12, AvaliacaoStatus.ENVIADA, "6.50");
        avaliador(13, AvaliacaoStatus.ENVIADA, "8.80");

        ResultadoConsolidacao r = service.consolidar(1L);

        // (8.50 + 6.50 + 8.80) / 3 = 7.933... -> 7.93
        assertThat(r.notaDoProjeto()).isEqualByComparingTo("7.93");
        assertThat(r.avaliado()).isTrue();
        assertThat(r.avaliadoresAtribuidos()).isEqualTo(3);
        assertThat(r.avaliacoesEnviadas()).isEqualTo(3);
        assertThat(r.notasPorAvaliador()).hasSize(3);
    }

    @Test
    void divergenciaEhAMaiorMenosAMenorNota() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.50");
        avaliador(12, AvaliacaoStatus.ENVIADA, "6.50");
        avaliador(13, AvaliacaoStatus.ENVIADA, "8.80");

        ResultadoConsolidacao r = service.consolidar(1L);

        assertThat(r.divergencia()).isEqualByComparingTo("2.30");
        assertThat(r.divergenciaAcimaDoLimite()).isFalse();
    }

    @Test
    void divergenciaAcimaDoLimiteDaEdicaoEhSinalizadaAoAdmin() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "9.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "5.00");
        avaliador(13, AvaliacaoStatus.ENVIADA, "7.00");

        ResultadoConsolidacao r = service.consolidar(1L);

        assertThat(r.divergencia()).isEqualByComparingTo("4.00");
        assertThat(r.divergenciaAcimaDoLimite()).isTrue();
    }

    @Test
    void semLimiteConfiguradoNaoHaAlertaDeDivergencia() {
        edicao.setLimiteDivergenciaNotas(null);
        avaliador(11, AvaliacaoStatus.ENVIADA, "10.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "0.00");
        avaliador(13, AvaliacaoStatus.ENVIADA, "5.00");

        assertThat(service.consolidar(1L).divergenciaAcimaDoLimite()).isFalse();
    }

    @Test
    void projetoSoEstaAvaliadoQuandoTodosOsAtribuidosEnviaram() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "7.00");
        avaliador(13, AvaliacaoStatus.RASCUNHO, null);

        ResultadoConsolidacao r = service.consolidar(1L);

        assertThat(r.avaliado()).isFalse();
        assertThat(r.avaliacoesEnviadas()).isEqualTo(2);
        assertThat(r.notaDoProjeto()).isEqualByComparingTo("7.50");
    }

    @Test
    void avaliadorSemAvaliacaoIniciadaImpedeOStatusAvaliado() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "7.00");
        avaliador(13, null, null);

        assertThat(service.consolidar(1L).avaliado()).isFalse();
    }

    @Test
    void menosAvaliadoresQueOMinimoDaEdicaoNuncaFicaAvaliado() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "7.00");

        ResultadoConsolidacao r = service.consolidar(1L);

        assertThat(r.avaliado()).isFalse();
        assertThat(r.notaDoProjeto()).isEqualByComparingTo("7.50");
    }

    @Test
    void rascunhoNaoEntraNaMedia() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.RASCUNHO, "2.00");
        avaliador(13, AvaliacaoStatus.ENVIADA, "6.00");

        assertThat(service.consolidar(1L).notaDoProjeto()).isEqualByComparingTo("7.00");
    }

    @Test
    void semNenhumParecerEnviadoNaoHaNotaNemDivergencia() {
        avaliador(11, null, null);
        avaliador(12, AvaliacaoStatus.RASCUNHO, null);

        ResultadoConsolidacao r = service.consolidar(1L);

        assertThat(r.notaDoProjeto()).isNull();
        assertThat(r.divergencia()).isNull();
        assertThat(r.avaliado()).isFalse();
    }

    @Test
    void comUmSoParecerNaoHaDivergencia() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");

        assertThat(service.consolidar(1L).divergencia()).isNull();
    }

    @Test
    void atribuicoesCanceladasNaoEntramNaConsolidacao() {
        // o repositório só devolve as ATIVA; garante que o serviço pergunta por elas
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");

        service.consolidar(1L);

        verify(atribuicaoRepository).findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA);
    }

    @Test
    void consolidarProjetoInexistenteLancaNaoEncontrado() {
        when(projetoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.consolidar(99L)).isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    // ---- status do projeto ----

    @Test
    void atualizarStatusMarcaComoAvaliadoQuandoTodosEnviaram() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "7.00");
        avaliador(13, AvaliacaoStatus.ENVIADA, "9.00");

        service.atualizarStatus(1L);

        assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.AVALIADO);
        verify(projetoRepository).save(projeto);
    }

    @Test
    void atualizarStatusMarcaEmAvaliacaoQuandoFaltaParecer() {
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.RASCUNHO, null);
        avaliador(13, null, null);

        service.atualizarStatus(1L);

        assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.EM_AVALIACAO);
    }

    @Test
    void reaberturaFazOProjetoVoltarDeAvaliadoParaEmAvaliacao() {
        projeto.setStatus(ProjetoStatus.AVALIADO);
        avaliador(11, AvaliacaoStatus.ENVIADA, "8.00");
        avaliador(12, AvaliacaoStatus.ENVIADA, "7.00");
        avaliador(13, AvaliacaoStatus.RASCUNHO, null);

        service.atualizarStatus(1L);

        assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.EM_AVALIACAO);
    }

    @Test
    void atualizarStatusNaoGravaQuandoNadaMudou() {
        projeto.setStatus(ProjetoStatus.EM_AVALIACAO);
        avaliador(11, AvaliacaoStatus.RASCUNHO, null);

        service.atualizarStatus(1L);

        verify(projetoRepository, never()).save(any());
    }

    @Test
    void semAtribuicoesOStatusAtualEhMantido() {
        projeto.setStatus(ProjetoStatus.SUBMETIDO);

        service.atualizarStatus(1L);

        assertThat(projeto.getStatus()).isEqualTo(ProjetoStatus.SUBMETIDO);
        verify(projetoRepository, never()).save(any());
    }
}
