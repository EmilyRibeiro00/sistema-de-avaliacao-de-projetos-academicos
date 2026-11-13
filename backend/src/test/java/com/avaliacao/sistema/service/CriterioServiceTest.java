package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.CriterioRepository;
import com.avaliacao.sistema.service.dto.CriterioCommand;
import java.math.BigDecimal;
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
class CriterioServiceTest {

    @Mock CriterioRepository criterioRepository;
    @Mock EdicaoService edicaoService;
    @Mock AuditoriaService auditoriaService;

    CriterioService service;
    Edicao edicao;

    @BeforeEach
    void setUp() {
        service = new CriterioService(criterioRepository, edicaoService, auditoriaService);
        edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoService.buscar(1L)).thenReturn(edicao);
        // edicaoService é mock: reproduz aqui o comportamento real de garantirEditavel, usado pelas
        // regras de "edição encerrada" abaixo.
        doAnswer(inv -> {
            Edicao e = inv.getArgument(0);
            if (e.getStatus() == EdicaoStatus.ENCERRADA) {
                throw new RegraNegocioException("Edição encerrada é somente leitura");
            }
            return null;
        }).when(edicaoService).garantirEditavel(any());
        when(criterioRepository.save(any(Criterio.class))).thenAnswer(i -> {
            Criterio c = i.getArgument(0);
            if (c.getId() == null) {
                c.setId(10L);
            }
            return c;
        });
    }

    @Test
    void adicionarCriaCriterioNaEdicaoEGravaAuditoria() {
        when(criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(1L)).thenReturn(List.of());

        Criterio criado = service.adicionar(1L, new CriterioCommand("Clareza", BigDecimal.valueOf(30), null), 9L);

        assertThat(criado.getEdicao()).isSameAs(edicao);
        assertThat(criado.getNome()).isEqualTo("Clareza");
        assertThat(criado.getOrdem()).isEqualTo(1);
        assertThat(criado.getAtivo()).isTrue();
        verify(auditoriaService).registrar(eq(9L), eq("CRITERIO_CRIADO"), eq("Criterio"), eq(10L), eq("Clareza"));
    }

    @Test
    void ordemPadraoContinuaDepoisDosCriteriosExistentes() {
        Criterio existente = TestData.criterio(5L, edicao, "Clareza", 30);
        when(criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(1L)).thenReturn(List.of(existente));

        Criterio criado = service.adicionar(1L, new CriterioCommand("Rigor", BigDecimal.valueOf(40), null), 9L);

        assertThat(criado.getOrdem()).isEqualTo(2);
    }

    @Test
    void pesoZeroOuNegativoEhRejeitado() {
        assertThatThrownBy(() -> service.adicionar(1L, new CriterioCommand("X", BigDecimal.ZERO, 1), 9L))
            .isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> service.adicionar(1L, new CriterioCommand("X", BigDecimal.valueOf(-5), 1), 9L))
            .isInstanceOf(RegraNegocioException.class);
        verify(criterioRepository, never()).save(any());
    }

    @Test
    void naoAdicionaCriterioEmEdicaoEncerrada() {
        edicao.setStatus(EdicaoStatus.ENCERRADA);

        assertThatThrownBy(() -> service.adicionar(1L, new CriterioCommand("X", BigDecimal.TEN, 1), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("somente leitura");
    }

    @Test
    void atualizarAlteraNomeEPesoEGravaAuditoria() {
        Criterio criterio = TestData.criterio(7L, edicao, "Relevância", 30);
        when(criterioRepository.findById(7L)).thenReturn(Optional.of(criterio));

        Criterio atualizado = service.atualizar(7L, new CriterioCommand("Relevância social", BigDecimal.valueOf(35), null), 9L);

        assertThat(atualizado.getNome()).isEqualTo("Relevância social");
        assertThat(atualizado.getPeso()).isEqualByComparingTo("35");
        verify(auditoriaService).registrar(eq(9L), eq("CRITERIO_ATUALIZADO"), eq("Criterio"), eq(7L), eq("Relevância social"));
    }

    @Test
    void atualizarComPesoInvalidoEhRejeitado() {
        when(criterioRepository.findById(7L)).thenReturn(Optional.of(TestData.criterio(7L, edicao, "X", 10)));

        assertThatThrownBy(() -> service.atualizar(7L, new CriterioCommand("X", BigDecimal.ZERO, null), 9L))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atualizarCriterioDeEdicaoEncerradaEhRejeitado() {
        edicao.setStatus(EdicaoStatus.ENCERRADA);
        when(criterioRepository.findById(7L)).thenReturn(Optional.of(TestData.criterio(7L, edicao, "X", 10)));

        assertThatThrownBy(() -> service.atualizar(7L, new CriterioCommand("X", BigDecimal.TEN, null), 9L))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removerMarcaInativoEmVezDeApagarEGravaAuditoria() {
        Criterio criterio = TestData.criterio(7L, edicao, "Relevância", 30);
        when(criterioRepository.findById(7L)).thenReturn(Optional.of(criterio));

        service.remover(7L, 9L);

        assertThat(criterio.getAtivo()).isFalse();
        verify(criterioRepository).save(criterio);
        verify(criterioRepository, never()).delete(any());
        verify(auditoriaService).registrar(eq(9L), eq("CRITERIO_REMOVIDO"), eq("Criterio"), eq(7L), eq("Relevância"));
    }

    @Test
    void removerCriterioDeEdicaoEncerradaEhRejeitado() {
        edicao.setStatus(EdicaoStatus.ENCERRADA);
        when(criterioRepository.findById(7L)).thenReturn(Optional.of(TestData.criterio(7L, edicao, "X", 10)));

        assertThatThrownBy(() -> service.remover(7L, 9L)).isInstanceOf(RegraNegocioException.class);
        verify(criterioRepository, never()).save(any());
    }

    @Test
    void removerInexistenteLancaNaoEncontrado() {
        when(criterioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remover(99L, 9L)).isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void somaPesosSomaTodosOsCriteriosDaEdicao() {
        when(criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(1L)).thenReturn(List.of(
            TestData.criterio(1L, edicao, "Clareza", 30),
            TestData.criterio(2L, edicao, "Rigor", 40),
            TestData.criterio(3L, edicao, "Relevância", 30)));

        assertThat(service.somaPesos(1L)).isEqualByComparingTo("100");
    }

    @Test
    void somaPesosSemCriteriosEhZero() {
        when(criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(1L)).thenReturn(List.of());

        assertThat(service.somaPesos(1L)).isEqualByComparingTo("0");
    }
}
