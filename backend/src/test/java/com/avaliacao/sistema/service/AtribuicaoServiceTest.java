package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Avaliacao;
import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.AtribuicaoOrigem;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
import com.avaliacao.sistema.entity.enums.EdicaoAvaliadorStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoDiscentePapel;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.AtribuicaoRepository;
import com.avaliacao.sistema.repository.AvaliacaoRepository;
import com.avaliacao.sistema.repository.EdicaoAvaliadorRepository;
import com.avaliacao.sistema.repository.ProjetoDiscenteRepository;
import com.avaliacao.sistema.repository.ProjetoOrientadorRepository;
import com.avaliacao.sistema.repository.ProjetoRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.ResultadoAtribuicao;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
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
class AtribuicaoServiceTest {

    @Mock ProjetoRepository projetoRepository;
    @Mock AtribuicaoRepository atribuicaoRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EdicaoAvaliadorRepository edicaoAvaliadorRepository;
    @Mock ProjetoOrientadorRepository projetoOrientadorRepository;
    @Mock ProjetoDiscenteRepository projetoDiscenteRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EdicaoService edicaoService;
    @Mock AuditoriaService auditoriaService;

    AtribuicaoService service;
    Edicao edicao;
    Comite comite;
    Campus campus;
    Projeto projeto;
    List<EdicaoAvaliador> participacoes;
    AtomicLong proximoId;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
        service = new AtribuicaoService(projetoRepository, atribuicaoRepository, avaliacaoRepository,
            edicaoAvaliadorRepository, projetoOrientadorRepository, projetoDiscenteRepository,
            usuarioRepository, edicaoService, auditoriaService, new Random(42), clock);

        edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        comite = TestData.comite(1L);
        campus = TestData.campus(1L);
        projeto = TestData.projeto(1L, edicao, comite, campus);
        participacoes = new ArrayList<>();
        proximoId = new AtomicLong(100);

        when(projetoRepository.findById(1L)).thenReturn(Optional.of(projeto));
        when(atribuicaoRepository.findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA)).thenReturn(List.of());
        when(edicaoAvaliadorRepository.findByEdicaoIdAndStatus(1L, EdicaoAvaliadorStatus.ATIVO)).thenReturn(participacoes);
        when(projetoOrientadorRepository.findByProjetoId(1L)).thenReturn(List.of());
        when(projetoDiscenteRepository.findByProjetoId(1L)).thenReturn(List.of());
        when(atribuicaoRepository.save(any(Atribuicao.class))).thenAnswer(i -> {
            Atribuicao a = i.getArgument(0);
            if (a.getId() == null) {
                a.setId(proximoId.incrementAndGet());
            }
            return a;
        });
    }

    /** Avaliador ativo na edição, no mesmo comitê e campus do projeto de teste — portanto elegível. */
    private Usuario elegivel(long id) {
        Usuario u = TestData.avaliador(id);
        u.setComite(comite);
        participacoes.add(TestData.participacao(edicao, u, null, campus));
        return u;
    }

    @Test
    void sorteiaOMinimoDaEdicaoEntreOsAvaliadoresDoMesmoComite() {
        for (long id = 11; id <= 15; id++) {
            elegivel(id);
        }

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.novosAtribuidos()).isEqualTo(3);
        assertThat(r.faltantes()).isZero();
        assertThat(r.completo()).isTrue();
        assertThat(r.avaliadoresSorteados()).doesNotHaveDuplicates().hasSize(3);

        ArgumentCaptor<Atribuicao> capturada = ArgumentCaptor.forClass(Atribuicao.class);
        verify(atribuicaoRepository, times(3)).save(capturada.capture());
        assertThat(capturada.getAllValues()).allSatisfy(a -> {
            assertThat(a.getStatus()).isEqualTo(AtribuicaoStatus.ATIVA);
            assertThat(a.getOrigem()).isEqualTo(AtribuicaoOrigem.AUTOMATICA);
            assertThat(a.getProjeto()).isSameAs(projeto);
        });
        verify(auditoriaService, times(3)).registrar(isNull(), eq("ATRIBUICAO_AUTOMATICA"), eq("Atribuicao"), anyLong(), any());
    }

    @Test
    void ignoraAvaliadoresDeOutroComite() {
        elegivel(11);
        elegivel(12);
        elegivel(13);
        Usuario deOutroComite = TestData.avaliador(14);
        deOutroComite.setComite(TestData.comite(2L));
        participacoes.add(TestData.participacao(edicao, deOutroComite, null, campus));

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.avaliadoresSorteados()).containsExactlyInAnyOrder(11L, 12L, 13L);
    }

    @Test
    void ignoraAvaliadoresDeOutroCampus() {
        elegivel(11);
        elegivel(12);
        elegivel(13);
        Usuario deOutroCampus = TestData.avaliador(14);
        deOutroCampus.setComite(comite);
        participacoes.add(TestData.participacao(edicao, deOutroCampus, null, TestData.campus(2L)));

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.avaliadoresSorteados()).containsExactlyInAnyOrder(11L, 12L, 13L);
    }

    @Test
    void excluiOrientadorECoautorPorConflitoDeInteresse() {
        for (long id = 11; id <= 15; id++) {
            elegivel(id);
        }
        when(projetoOrientadorRepository.findByProjetoId(1L)).thenReturn(List.of(TestData.orientador(projeto, 11L)));
        when(projetoDiscenteRepository.findByProjetoId(1L))
            .thenReturn(List.of(TestData.discente(projeto, 12L, ProjetoDiscentePapel.COAUTOR)));

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.avaliadoresSorteados()).containsExactlyInAnyOrder(13L, 14L, 15L);
        assertThat(r.avisos()).anyMatch(a -> a.contains("conflito de interesse"));
    }

    @Test
    void orientadorDeUmProjetoPodeAvaliarOutro() {
        Projeto outroProjeto = TestData.projeto(2L, edicao, comite, campus);
        when(projetoRepository.findById(2L)).thenReturn(Optional.of(outroProjeto));
        when(atribuicaoRepository.findByProjetoIdAndStatus(2L, AtribuicaoStatus.ATIVA)).thenReturn(List.of());
        when(projetoOrientadorRepository.findByProjetoId(2L)).thenReturn(List.of());
        when(projetoDiscenteRepository.findByProjetoId(2L)).thenReturn(List.of());
        // a pessoa 11 orienta o projeto 1, mas nada a impede de avaliar o projeto 2
        when(projetoOrientadorRepository.findByProjetoId(1L)).thenReturn(List.of(TestData.orientador(projeto, 11L)));
        for (long id = 11; id <= 13; id++) {
            elegivel(id);
        }

        ResultadoAtribuicao r = service.atribuirProjeto(2L);

        assertThat(r.avaliadoresSorteados()).containsExactlyInAnyOrder(11L, 12L, 13L);
    }

    @Test
    void respeitaOLimiteDeCargaDaEdicao() {
        edicao.setLimiteMaxProjetosPorAvaliador(2);
        for (long id = 11; id <= 14; id++) {
            elegivel(id);
        }
        when(atribuicaoRepository.countByAvaliadorIdAndStatusAndProjetoEdicaoId(11L, AtribuicaoStatus.ATIVA, 1L)).thenReturn(2);

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.avaliadoresSorteados()).containsExactlyInAnyOrder(12L, 13L, 14L);
    }

    @Test
    void limiteIndividualDoAvaliadorTemPrioridadeSobreODaEdicao() {
        edicao.setLimiteMaxProjetosPorAvaliador(10);
        for (long id = 11; id <= 14; id++) {
            elegivel(id);
        }
        participacoes.get(0).setLimiteMaxProjetos(1);
        when(atribuicaoRepository.countByAvaliadorIdAndStatusAndProjetoEdicaoId(11L, AtribuicaoStatus.ATIVA, 1L)).thenReturn(1);

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.avaliadoresSorteados()).doesNotContain(11L);
    }

    @Test
    void poolInsuficienteSinalizaOsFaltantesEmVezDeFalharEmSilencio() {
        elegivel(11);
        elegivel(12);

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.novosAtribuidos()).isEqualTo(2);
        assertThat(r.faltantes()).isEqualTo(1);
        assertThat(r.completo()).isFalse();
        assertThat(r.avisos()).anyMatch(a -> a.contains("Pool insuficiente"));
        verify(auditoriaService).registrar(isNull(), eq("ATRIBUICAO_INCOMPLETA"), eq("Projeto"), eq(1L), contains("faltam 1"));
    }

    @Test
    void semNenhumElegivelNaoAtribuiNadaMasSinaliza() {
        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.novosAtribuidos()).isZero();
        assertThat(r.faltantes()).isEqualTo(3);
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    void atribuicoesAtivasContamParaOMinimoENaoSaoRepetidas() {
        Usuario jaAtribuido1 = elegivel(11);
        Usuario jaAtribuido2 = elegivel(12);
        elegivel(13);
        elegivel(14);
        when(atribuicaoRepository.findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA)).thenReturn(List.of(
            TestData.atribuicao(1L, projeto, jaAtribuido1, AtribuicaoStatus.ATIVA),
            TestData.atribuicao(2L, projeto, jaAtribuido2, AtribuicaoStatus.ATIVA)));

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.jaAtribuidos()).isEqualTo(2);
        assertThat(r.novosAtribuidos()).isEqualTo(1);
        assertThat(r.avaliadoresSorteados()).hasSize(1).doesNotContain(11L, 12L);
    }

    @Test
    void projetoJaCompletoNaoRecebeNovasAtribuicoes() {
        Usuario a = elegivel(11);
        Usuario b = elegivel(12);
        Usuario c = elegivel(13);
        elegivel(14);
        when(atribuicaoRepository.findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA)).thenReturn(List.of(
            TestData.atribuicao(1L, projeto, a, AtribuicaoStatus.ATIVA),
            TestData.atribuicao(2L, projeto, b, AtribuicaoStatus.ATIVA),
            TestData.atribuicao(3L, projeto, c, AtribuicaoStatus.ATIVA)));

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.novosAtribuidos()).isZero();
        assertThat(r.completo()).isTrue();
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    void naoAtribuiEmEdicaoQueNaoEstaAtiva() {
        edicao.setStatus(EdicaoStatus.ENCERRADA);

        assertThatThrownBy(() -> service.atribuirProjeto(1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("edição ativa");
    }

    @Test
    void projetoSemComiteNaoTemComoFiltrarEEhSinalizado() {
        projeto.setComite(null);
        elegivel(11);

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.faltantes()).isEqualTo(3);
        assertThat(r.avisos()).anyMatch(a -> a.contains("sem comitê"));
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    void projetoSemCampusNaoTemComoFiltrarEEhSinalizado() {
        projeto.setCampus(null);
        elegivel(11);

        ResultadoAtribuicao r = service.atribuirProjeto(1L);

        assertThat(r.faltantes()).isEqualTo(3);
        assertThat(r.avisos()).anyMatch(a -> a.contains("sem campus"));
        verify(atribuicaoRepository, never()).save(any());
    }

    @Test
    void atribuirEdicaoAtivaPercorreTodosOsProjetosDaEdicao() {
        Projeto p2 = TestData.projeto(2L, edicao, comite, campus);
        when(edicaoService.buscarAtiva()).thenReturn(edicao);
        when(projetoRepository.findByEdicaoIdAndAtivoTrue(1L)).thenReturn(List.of(projeto, p2));
        when(projetoRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(atribuicaoRepository.findByProjetoIdAndStatus(2L, AtribuicaoStatus.ATIVA)).thenReturn(List.of());
        when(projetoOrientadorRepository.findByProjetoId(2L)).thenReturn(List.of());
        when(projetoDiscenteRepository.findByProjetoId(2L)).thenReturn(List.of());
        for (long id = 11; id <= 13; id++) {
            elegivel(id);
        }

        List<ResultadoAtribuicao> resultados = service.atribuirEdicaoAtiva();

        assertThat(resultados).extracting(ResultadoAtribuicao::projetoId).containsExactly(1L, 2L);
        assertThat(resultados).allMatch(ResultadoAtribuicao::completo);
    }

    // ---- reatribuição manual ----

    private Atribuicao atribuicaoAtiva(long id, Usuario avaliador) {
        Atribuicao a = TestData.atribuicao(id, projeto, avaliador, AtribuicaoStatus.ATIVA);
        when(atribuicaoRepository.findById(id)).thenReturn(Optional.of(a));
        return a;
    }

    /** Avaliador apto a ser o novo (mesmo comitê e campus do projeto de teste), já participando da edição. */
    private Usuario novoAvaliadorNaEdicao(long id) {
        Usuario novo = TestData.avaliador(id);
        novo.setComite(comite);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(novo));
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(1L, id))
            .thenReturn(Optional.of(TestData.participacao(edicao, novo, null, campus)));
        return novo;
    }

    @Test
    void reatribuirExigeMotivo() {
        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "  ", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("motivo");
        assertThatThrownBy(() -> service.reatribuir(1L, 20L, null, 9L)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void reatribuirCancelaAAntigaECriaUmaManualComMotivoELog() {
        Usuario antigo = TestData.avaliador(11);
        Atribuicao antiga = atribuicaoAtiva(1L, antigo);
        Usuario novo = novoAvaliadorNaEdicao(20);
        Usuario admin = TestData.avaliador(9);
        when(usuarioRepository.getReferenceById(9L)).thenReturn(admin);

        Atribuicao nova = service.reatribuir(1L, 20L, "Avaliador indisponível", 9L);

        assertThat(antiga.getStatus()).isEqualTo(AtribuicaoStatus.CANCELADA);
        assertThat(nova.getStatus()).isEqualTo(AtribuicaoStatus.ATIVA);
        assertThat(nova.getOrigem()).isEqualTo(AtribuicaoOrigem.MANUAL);
        assertThat(nova.getAvaliador()).isSameAs(novo);
        assertThat(nova.getMotivo()).isEqualTo("Avaliador indisponível");
        assertThat(nova.getCriadoPor()).isSameAs(admin);
        verify(auditoriaService).registrar(eq(9L), eq("REATRIBUICAO_MANUAL"), eq("Atribuicao"), eq(nova.getId()), contains("Avaliador indisponível"));
    }

    @Test
    void reatribuirParaAvaliadorDeOutroComiteEhRejeitado() {
        atribuicaoAtiva(1L, TestData.avaliador(11));
        Usuario novo = TestData.avaliador(20);
        novo.setComite(TestData.comite(2L));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(novo));
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(1L, 20L))
            .thenReturn(Optional.of(TestData.participacao(edicao, novo, null, campus)));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("comitê");
    }

    @Test
    void reatribuirParaAvaliadorDeOutroCampusEhRejeitado() {
        atribuicaoAtiva(1L, TestData.avaliador(11));
        Usuario novo = TestData.avaliador(20);
        novo.setComite(comite);
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(novo));
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(1L, 20L))
            .thenReturn(Optional.of(TestData.participacao(edicao, novo, null, TestData.campus(2L))));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("campus");
    }

    @Test
    void reatribuirParaAutorOuOrientadorDoProjetoEhConflito() {
        atribuicaoAtiva(1L, TestData.avaliador(11));
        novoAvaliadorNaEdicao(20);
        when(projetoOrientadorRepository.findByProjetoId(1L)).thenReturn(List.of(TestData.orientador(projeto, 20L)));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("Conflito de interesse");
    }

    @Test
    void reatribuirParaAvaliadorForaDaEdicaoEhRejeitado() {
        atribuicaoAtiva(1L, TestData.avaliador(11));
        Usuario novo = TestData.avaliador(20);
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(novo));
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(1L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("não participa");
    }

    @Test
    void reatribuirParaQuemJaEstaNoProjetoEhRejeitado() {
        Usuario antigo = TestData.avaliador(11);
        atribuicaoAtiva(1L, antigo);
        Usuario novo = novoAvaliadorNaEdicao(20);
        when(atribuicaoRepository.findByProjetoIdAndStatus(1L, AtribuicaoStatus.ATIVA)).thenReturn(List.of(
            TestData.atribuicao(1L, projeto, antigo, AtribuicaoStatus.ATIVA),
            TestData.atribuicao(2L, projeto, novo, AtribuicaoStatus.ATIVA)));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("já está atribuído");
    }

    @Test
    void reatribuirComAvaliacaoJaEnviadaExigeReabertura() {
        atribuicaoAtiva(1L, TestData.avaliador(11));
        novoAvaliadorNaEdicao(20);
        Avaliacao enviada = new Avaliacao();
        enviada.setStatus(AvaliacaoStatus.ENVIADA);
        when(avaliacaoRepository.findByAtribuicaoId(1L)).thenReturn(Optional.of(enviada));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("reabra");
    }

    @Test
    void reatribuirAtribuicaoJaCanceladaEhRejeitado() {
        Atribuicao cancelada = TestData.atribuicao(1L, projeto, TestData.avaliador(11), AtribuicaoStatus.CANCELADA);
        when(atribuicaoRepository.findById(1L)).thenReturn(Optional.of(cancelada));

        assertThatThrownBy(() -> service.reatribuir(1L, 20L, "motivo", 9L)).isInstanceOf(RegraNegocioException.class);
    }
}
