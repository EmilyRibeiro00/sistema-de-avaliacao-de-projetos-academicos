package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.EdicaoRepository;
import com.avaliacao.sistema.service.dto.EdicaoCommand;
import java.math.BigDecimal;
import java.time.Instant;
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
class EdicaoServiceTest {

    private static final Instant ABERTURA = Instant.parse("2026-09-01T00:00:00Z");
    private static final Instant FECHAMENTO = Instant.parse("2026-10-15T00:00:00Z");

    @Mock EdicaoRepository edicaoRepository;
    @Mock AuditoriaService auditoriaService;

    EdicaoService service;

    @BeforeEach
    void setUp() {
        service = new EdicaoService(edicaoRepository, auditoriaService);
        when(edicaoRepository.save(any(Edicao.class))).thenAnswer(i -> i.getArgument(0));
    }

    private EdicaoCommand comando(Instant abertura, Instant fechamento, Integer minimo, BigDecimal escalaMin, BigDecimal escalaMax) {
        return new EdicaoCommand("Mostra 2026", 2026, abertura, fechamento, minimo, null, null, escalaMin, escalaMax);
    }

    @Test
    void criarAplicaOsPadroesDaRegraDeNegocio() {
        Edicao criada = service.criar(comando(ABERTURA, FECHAMENTO, null, null, null), 1L);

        assertThat(criada.getStatus()).isEqualTo(EdicaoStatus.PLANEJADA);
        assertThat(criada.getMinAvaliadoresPorProjeto()).isEqualTo(3);
        assertThat(criada.getEscalaNotaMin()).isEqualByComparingTo("0");
        assertThat(criada.getEscalaNotaMax()).isEqualByComparingTo("10");
        verify(auditoriaService).registrar(eq(1L), eq("EDICAO_CRIADA"), eq("Edicao"), any(), eq("Mostra 2026"));
    }

    @Test
    void criarRespeitaOMinimoInformado() {
        Edicao criada = service.criar(comando(ABERTURA, FECHAMENTO, 5, null, null), 1L);

        assertThat(criada.getMinAvaliadoresPorProjeto()).isEqualTo(5);
    }

    @Test
    void criarRejeitaFechamentoAntesDaAbertura() {
        assertThatThrownBy(() -> service.criar(comando(FECHAMENTO, ABERTURA, null, null, null), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("fechamento");
        verify(edicaoRepository, never()).save(any());
    }

    @Test
    void criarRejeitaMinimoMenorQueUm() {
        assertThatThrownBy(() -> service.criar(comando(ABERTURA, FECHAMENTO, 0, null, null), 1L))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void criarRejeitaEscalaInvertida() {
        assertThatThrownBy(() -> service.criar(
            comando(ABERTURA, FECHAMENTO, null, BigDecimal.TEN, BigDecimal.ZERO), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("escala");
    }

    @Test
    void ativarMarcaAEdicaoComoAtiva() {
        Edicao planejada = TestData.edicao(1L, EdicaoStatus.PLANEJADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(planejada));
        when(edicaoRepository.findByStatus(EdicaoStatus.ATIVA)).thenReturn(Optional.empty());

        Edicao ativada = service.ativar(1L, 9L);

        assertThat(ativada.getStatus()).isEqualTo(EdicaoStatus.ATIVA);
        verify(auditoriaService).registrar(eq(9L), eq("EDICAO_ATIVADA"), eq("Edicao"), eq(1L), any());
    }

    @Test
    void ativarFalhaQuandoJaExisteOutraEdicaoAtiva() {
        Edicao planejada = TestData.edicao(2L, EdicaoStatus.PLANEJADA, 3);
        Edicao outraAtiva = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoRepository.findById(2L)).thenReturn(Optional.of(planejada));
        when(edicaoRepository.findByStatus(EdicaoStatus.ATIVA)).thenReturn(Optional.of(outraAtiva));

        assertThatThrownBy(() -> service.ativar(2L, 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("Já existe uma edição ativa");
        assertThat(planejada.getStatus()).isEqualTo(EdicaoStatus.PLANEJADA);
    }

    @Test
    void ativarUmaEdicaoJaAtivaEhIdempotente() {
        Edicao ativa = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(ativa));
        when(edicaoRepository.findByStatus(EdicaoStatus.ATIVA)).thenReturn(Optional.of(ativa));

        assertThat(service.ativar(1L, 9L).getStatus()).isEqualTo(EdicaoStatus.ATIVA);
    }

    @Test
    void naoReativaEdicaoEncerrada() {
        Edicao encerrada = TestData.edicao(1L, EdicaoStatus.ENCERRADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(encerrada));

        assertThatThrownBy(() -> service.ativar(1L, 9L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void encerrarSoFuncionaParaEdicaoAtiva() {
        Edicao planejada = TestData.edicao(1L, EdicaoStatus.PLANEJADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(planejada));

        assertThatThrownBy(() -> service.encerrar(1L, 9L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void encerrarMudaOStatusEGravaAuditoria() {
        Edicao ativa = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(ativa));

        assertThat(service.encerrar(1L, 9L).getStatus()).isEqualTo(EdicaoStatus.ENCERRADA);
        verify(auditoriaService).registrar(eq(9L), eq("EDICAO_ENCERRADA"), eq("Edicao"), eq(1L), any());
    }

    @Test
    void atualizarAlteraOsCamposEGravaAuditoria() {
        Edicao planejada = TestData.edicao(1L, EdicaoStatus.PLANEJADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(planejada));

        Edicao atualizada = service.atualizar(1L, comando(ABERTURA, FECHAMENTO, 5, null, null), 9L);

        assertThat(atualizada.getMinAvaliadoresPorProjeto()).isEqualTo(5);
        assertThat(atualizada.getNome()).isEqualTo("Mostra 2026");
        verify(auditoriaService).registrar(eq(9L), eq("EDICAO_ATUALIZADA"), eq("Edicao"), eq(1L), any());
    }

    @Test
    void atualizarEdicaoEncerradaEhRejeitada() {
        Edicao encerrada = TestData.edicao(1L, EdicaoStatus.ENCERRADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(encerrada));

        assertThatThrownBy(() -> service.atualizar(1L, comando(ABERTURA, FECHAMENTO, null, null, null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("somente leitura");
    }

    @Test
    void removerMarcaInativaEGravaAuditoria() {
        Edicao planejada = TestData.edicao(1L, EdicaoStatus.PLANEJADA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(planejada));

        service.remover(1L, 9L);

        assertThat(planejada.getAtivo()).isFalse();
        verify(auditoriaService).registrar(eq(9L), eq("EDICAO_REMOVIDA"), eq("Edicao"), eq(1L), any());
    }

    @Test
    void removerEdicaoAtivaEhRejeitada() {
        Edicao ativa = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoRepository.findById(1L)).thenReturn(Optional.of(ativa));

        assertThatThrownBy(() -> service.remover(1L, 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("Encerre");
        assertThat(ativa.getAtivo()).isTrue();
    }

    @Test
    void listarSoDevolveEdicoesAtivas() {
        service.listar();

        verify(edicaoRepository).findByAtivoTrue();
    }

    @Test
    void buscarInexistenteLancaNaoEncontrada() {
        when(edicaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void buscarAtivaSemEdicaoAtivaLancaRegraDeNegocio() {
        when(edicaoRepository.findByStatus(EdicaoStatus.ATIVA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarAtiva())
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("Não há edição ativa");
    }
}
