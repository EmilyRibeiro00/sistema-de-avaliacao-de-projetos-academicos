package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Avaliacao;
import com.avaliacao.sistema.entity.AvaliacaoNota;
import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.exception.AcessoNegadoException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.AtribuicaoRepository;
import com.avaliacao.sistema.repository.AvaliacaoNotaRepository;
import com.avaliacao.sistema.repository.AvaliacaoRepository;
import com.avaliacao.sistema.repository.CriterioRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.AvaliacaoDetalhe;
import com.avaliacao.sistema.service.dto.ProjetoAvaliadorView;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AvaliacaoServiceTest {

    private static final Long AVALIADOR = 11L;
    private static final Long ATRIBUICAO = 50L;

    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock AvaliacaoNotaRepository avaliacaoNotaRepository;
    @Mock CriterioRepository criterioRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ConsolidacaoService consolidacaoService;
    @Mock AuditoriaService auditoriaService;

    AvaliacaoService service;
    Edicao edicao;
    Projeto projeto;
    Atribuicao atribuicao;
    Criterio clareza;
    Criterio rigor;
    Criterio relevancia;
    List<Avaliacao> avaliacoesSalvas;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
        service = new AvaliacaoService(atribuicaoRepository, avaliacaoRepository, avaliacaoNotaRepository,
            criterioRepository, usuarioRepository, consolidacaoService, auditoriaService, clock);

        edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        projeto = TestData.projeto(1L, edicao, TestData.comite(1L), TestData.campus(1L));
        atribuicao = TestData.atribuicao(ATRIBUICAO, projeto, TestData.avaliador(AVALIADOR), AtribuicaoStatus.ATIVA);
        clareza = TestData.criterio(1L, edicao, "Clareza", 30);
        rigor = TestData.criterio(2L, edicao, "Rigor Metodológico", 40);
        relevancia = TestData.criterio(3L, edicao, "Relevância", 30);
        avaliacoesSalvas = new ArrayList<>();

        when(atribuicaoRepository.findById(ATRIBUICAO)).thenReturn(Optional.of(atribuicao));
        when(atribuicaoRepository.findByAvaliadorIdAndStatus(AVALIADOR, AtribuicaoStatus.ATIVA)).thenReturn(List.of(atribuicao));
        when(criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(1L)).thenReturn(List.of(clareza, rigor, relevancia));
        when(avaliacaoRepository.findByAtribuicaoId(ATRIBUICAO)).thenReturn(Optional.empty());
        when(avaliacaoNotaRepository.findByAvaliacaoId(any())).thenReturn(List.of());
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(i -> {
            Avaliacao a = i.getArgument(0);
            if (a.getId() == null) {
                a.setId(200L);
            }
            avaliacoesSalvas.add(a);
            return a;
        });
    }

    private Avaliacao ultimaAvaliacaoSalva() {
        return avaliacoesSalvas.get(avaliacoesSalvas.size() - 1);
    }

    private static Map<Long, BigDecimal> notas(String clareza, String rigor, String relevancia) {
        return Map.of(1L, new BigDecimal(clareza), 2L, new BigDecimal(rigor), 3L, new BigDecimal(relevancia));
    }

    // ---- visão cega e posse ----

    @Test
    void listarProjetosDevolveSoOsAtribuidosAoAvaliador() {
        List<ProjetoAvaliadorView> views = service.listarProjetos(AVALIADOR);

        assertThat(views).hasSize(1);
        assertThat(views.get(0).titulo()).isEqualTo("Projeto 1");
        assertThat(views.get(0).comite()).isEqualTo("Comitê 1");
        assertThat(views.get(0).statusAvaliacao()).isEqualTo("PENDENTE");
    }

    @Test
    void visaoDoAvaliadorNaoTemCampoDeAutoriaNemInstituicao() {
        Set<String> campos = Stream.of(ProjetoAvaliadorView.class.getRecordComponents())
            .map(c -> c.getName().toLowerCase())
            .collect(Collectors.toSet());

        assertThat(campos).containsExactlyInAnyOrder(
            "atribuicaoid", "projetoid", "titulo", "comite", "conteudotecnico", "statusavaliacao");
        assertThat(campos).noneMatch(c -> c.contains("autor") || c.contains("orientador")
            || c.contains("instituicao") || c.contains("discente"));
    }

    @Test
    void avaliadorNaoAcessaAtribuicaoDeOutroAvaliador() {
        assertThatThrownBy(() -> service.detalhar(99L, ATRIBUICAO)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> service.salvarRascunho(99L, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> service.enviar(99L, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(AcessoNegadoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void atribuicaoCanceladaNaoEhMaisAcessivelAoAvaliador() {
        atribuicao.setStatus(AtribuicaoStatus.CANCELADA);

        assertThatThrownBy(() -> service.detalhar(AVALIADOR, ATRIBUICAO)).isInstanceOf(AcessoNegadoException.class);
    }

    // ---- rascunho ----

    @Test
    void rascunhoAceitaNotasParciaisENaoCalculaNotaFinal() {
        AvaliacaoDetalhe detalhe = service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(1L, new BigDecimal("8")));

        Avaliacao salva = ultimaAvaliacaoSalva();
        assertThat(salva.getStatus()).isEqualTo(AvaliacaoStatus.RASCUNHO);
        assertThat(salva.getNotaFinal()).isNull();
        assertThat(detalhe.criterios()).hasSize(3);
        verify(consolidacaoService, never()).atualizarStatus(any());

        ArgumentCaptor<Iterable<AvaliacaoNota>> notasSalvas = ArgumentCaptor.forClass(Iterable.class);
        verify(avaliacaoNotaRepository).saveAll(notasSalvas.capture());
        assertThat(notasSalvas.getValue()).hasSize(1);
    }

    @Test
    void rascunhoAtualizaNotaJaExistenteEmVezDeDuplicar() {
        Avaliacao existente = new Avaliacao();
        existente.setId(200L);
        existente.setAtribuicao(atribuicao);
        existente.setStatus(AvaliacaoStatus.RASCUNHO);
        AvaliacaoNota notaAntiga = new AvaliacaoNota();
        notaAntiga.setAvaliacao(existente);
        notaAntiga.setCriterio(clareza);
        notaAntiga.setNota(new BigDecimal("5"));
        when(avaliacaoRepository.findByAtribuicaoId(ATRIBUICAO)).thenReturn(Optional.of(existente));
        when(avaliacaoNotaRepository.findByAvaliacaoId(200L)).thenReturn(List.of(notaAntiga));

        service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(1L, new BigDecimal("9")));

        assertThat(notaAntiga.getNota()).isEqualByComparingTo("9");
        ArgumentCaptor<Iterable<AvaliacaoNota>> notasSalvas = ArgumentCaptor.forClass(Iterable.class);
        verify(avaliacaoNotaRepository).saveAll(notasSalvas.capture());
        assertThat(notasSalvas.getValue()).containsExactly(notaAntiga);
    }

    // ---- validações de nota e prazo ----

    @Test
    void notaForaDaEscalaEhRejeitada() {
        assertThatThrownBy(() -> service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(1L, new BigDecimal("10.5"))))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("entre");
        assertThatThrownBy(() -> service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(1L, new BigDecimal("-1"))))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void notasNosLimitesDaEscalaSaoAceitas() {
        service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(1L, BigDecimal.ZERO, 2L, BigDecimal.TEN));

        verify(avaliacaoNotaRepository).saveAll(anyIterable());
    }

    @Test
    void criterioDeOutraEdicaoEhRejeitado() {
        assertThatThrownBy(() -> service.salvarRascunho(AVALIADOR, ATRIBUICAO, Map.of(999L, new BigDecimal("5"))))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("não pertence");
    }

    @Test
    void foraDoPrazoNaoPermiteRascunhoNemEnvio() {
        edicao.setDataFechamento(Instant.parse("2026-09-30T00:00:00Z"));

        assertThatThrownBy(() -> service.salvarRascunho(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("prazo");
        assertThatThrownBy(() -> service.enviar(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void antesDaAberturaTambemEstaForaDoPrazo() {
        edicao.setDataAbertura(Instant.parse("2026-10-02T00:00:00Z"));

        assertThatThrownBy(() -> service.enviar(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void edicaoEncerradaNaoPermiteAvaliar() {
        edicao.setStatus(EdicaoStatus.ENCERRADA);

        assertThatThrownBy(() -> service.enviar(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void detalheForaDoPrazoVeioSomenteLeitura() {
        edicao.setDataFechamento(Instant.parse("2026-09-30T00:00:00Z"));

        assertThat(service.detalhar(AVALIADOR, ATRIBUICAO).editavel()).isFalse();
    }

    // ---- envio final ----

    @Test
    void enviarCalculaAMediaPonderadaEMarcaComoEnviada() {
        AvaliacaoDetalhe detalhe = service.enviar(AVALIADOR, ATRIBUICAO, notas("8", "9", "7"));

        // (8x30 + 9x40 + 7x30) / 100 = 8.10
        Avaliacao enviada = ultimaAvaliacaoSalva();
        assertThat(enviada.getStatus()).isEqualTo(AvaliacaoStatus.ENVIADA);
        assertThat(enviada.getNotaFinal()).isEqualByComparingTo("8.10");
        assertThat(enviada.getDataEnvio()).isEqualTo(Instant.parse("2026-10-01T12:00:00Z"));
        assertThat(detalhe).isNotNull();
        verify(consolidacaoService).atualizarStatus(1L);
        verify(auditoriaService).registrar(eq(AVALIADOR), eq("AVALIACAO_ENVIADA"), eq("Avaliacao"), eq(200L), any());
    }

    @Test
    void enviarExigeTodosOsCriterios() {
        assertThatThrownBy(() -> service.enviar(AVALIADOR, ATRIBUICAO,
            Map.of(1L, new BigDecimal("8"), 2L, new BigDecimal("9"))))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("Todos os critérios são obrigatórios")
            .hasMessageContaining("Relevância");
        verify(consolidacaoService, never()).atualizarStatus(any());
    }

    @Test
    void enviarUsaNotasDoRascunhoAnteriorParaCompletarOsCriterios() {
        Avaliacao rascunho = new Avaliacao();
        rascunho.setId(200L);
        rascunho.setAtribuicao(atribuicao);
        rascunho.setStatus(AvaliacaoStatus.RASCUNHO);
        AvaliacaoNota rigorSalvo = new AvaliacaoNota();
        rigorSalvo.setCriterio(rigor);
        rigorSalvo.setNota(new BigDecimal("9"));
        AvaliacaoNota relevanciaSalva = new AvaliacaoNota();
        relevanciaSalva.setCriterio(relevancia);
        relevanciaSalva.setNota(new BigDecimal("7"));
        when(avaliacaoRepository.findByAtribuicaoId(ATRIBUICAO)).thenReturn(Optional.of(rascunho));
        when(avaliacaoNotaRepository.findByAvaliacaoId(200L)).thenReturn(List.of(rigorSalvo, relevanciaSalva));

        service.enviar(AVALIADOR, ATRIBUICAO, Map.of(1L, new BigDecimal("8")));

        assertThat(rascunho.getStatus()).isEqualTo(AvaliacaoStatus.ENVIADA);
        assertThat(rascunho.getNotaFinal()).isEqualByComparingTo("8.10");
    }

    @Test
    void avaliacaoEnviadaNaoPodeMaisSerEditadaPeloAvaliador() {
        Avaliacao enviada = new Avaliacao();
        enviada.setId(200L);
        enviada.setAtribuicao(atribuicao);
        enviada.setStatus(AvaliacaoStatus.ENVIADA);
        when(avaliacaoRepository.findByAtribuicaoId(ATRIBUICAO)).thenReturn(Optional.of(enviada));

        assertThatThrownBy(() -> service.salvarRascunho(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("já enviada");
        assertThatThrownBy(() -> service.enviar(AVALIADOR, ATRIBUICAO, notas("5", "5", "5")))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void detalheDeAvaliacaoEnviadaNaoEhEditavel() {
        Avaliacao enviada = new Avaliacao();
        enviada.setId(200L);
        enviada.setAtribuicao(atribuicao);
        enviada.setStatus(AvaliacaoStatus.ENVIADA);
        enviada.setNotaFinal(new BigDecimal("8.10"));
        when(avaliacaoRepository.findByAtribuicaoId(ATRIBUICAO)).thenReturn(Optional.of(enviada));

        AvaliacaoDetalhe detalhe = service.detalhar(AVALIADOR, ATRIBUICAO);

        assertThat(detalhe.editavel()).isFalse();
        assertThat(detalhe.notaFinal()).isEqualByComparingTo("8.10");
        assertThat(detalhe.projeto().statusAvaliacao()).isEqualTo("ENVIADA");
    }

    // ---- nota final ----

    @Test
    void calcularNotaFinalEhMediaPonderadaComDuasCasas() {
        List<Criterio> criterios = List.of(clareza, rigor, relevancia);

        assertThat(AvaliacaoService.calcularNotaFinal(criterios, notas("8", "9", "7"))).isEqualByComparingTo("8.10");
        assertThat(AvaliacaoService.calcularNotaFinal(criterios, notas("10", "10", "10"))).isEqualByComparingTo("10.00");
        assertThat(AvaliacaoService.calcularNotaFinal(criterios, notas("0", "0", "0"))).isEqualByComparingTo("0.00");
        assertThat(AvaliacaoService.calcularNotaFinal(criterios, notas("7", "8", "6.5"))).isEqualByComparingTo("7.25");
    }

    @Test
    void calcularNotaFinalFuncionaComPesosQueNaoSomamCem() {
        Criterio a = TestData.criterio(1L, edicao, "A", 1);
        Criterio b = TestData.criterio(2L, edicao, "B", 3);

        BigDecimal nota = AvaliacaoService.calcularNotaFinal(List.of(a, b),
            Map.of(1L, new BigDecimal("4"), 2L, new BigDecimal("8")));

        // (4x1 + 8x3) / 4 = 7
        assertThat(nota).isEqualByComparingTo("7.00");
    }

    // ---- reabertura (admin) ----

    @Test
    void reabrirExigeMotivo() {
        assertThatThrownBy(() -> service.reabrir(200L, " ", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("motivo");
    }

    @Test
    void reabrirVoltaParaRascunhoLimpaANotaERegistraJustificativa() {
        Avaliacao enviada = new Avaliacao();
        enviada.setId(200L);
        enviada.setAtribuicao(atribuicao);
        enviada.setStatus(AvaliacaoStatus.ENVIADA);
        enviada.setNotaFinal(new BigDecimal("8.10"));
        enviada.setDataEnvio(Instant.parse("2026-09-20T00:00:00Z"));
        Usuario admin = TestData.avaliador(9);
        when(avaliacaoRepository.findById(200L)).thenReturn(Optional.of(enviada));
        when(usuarioRepository.getReferenceById(9L)).thenReturn(admin);

        Avaliacao reaberta = service.reabrir(200L, "Erro de digitação numa nota", 9L);

        assertThat(reaberta.getStatus()).isEqualTo(AvaliacaoStatus.RASCUNHO);
        assertThat(reaberta.getNotaFinal()).isNull();
        assertThat(reaberta.getDataEnvio()).isNull();
        assertThat(reaberta.getMotivoReabertura()).isEqualTo("Erro de digitação numa nota");
        assertThat(reaberta.getReabertaPor()).isSameAs(admin);
        assertThat(reaberta.getReabertaEm()).isEqualTo(Instant.parse("2026-10-01T12:00:00Z"));
        verify(consolidacaoService).atualizarStatus(1L);
        verify(auditoriaService).registrar(eq(9L), eq("AVALIACAO_REABERTA"), eq("Avaliacao"), eq(200L), eq("Erro de digitação numa nota"));
    }

    @Test
    void soAvaliacaoEnviadaPodeSerReaberta() {
        Avaliacao rascunho = new Avaliacao();
        rascunho.setId(200L);
        rascunho.setAtribuicao(atribuicao);
        rascunho.setStatus(AvaliacaoStatus.RASCUNHO);
        when(avaliacaoRepository.findById(200L)).thenReturn(Optional.of(rascunho));

        assertThatThrownBy(() -> service.reabrir(200L, "motivo", 9L)).isInstanceOf(RegraNegocioException.class);
    }
}
