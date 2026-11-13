package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.EdicaoAvaliadorStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.EdicaoAvaliadorRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.VincularAvaliadorCommand;
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
class EdicaoAvaliadorServiceTest {

    @Mock EdicaoAvaliadorRepository edicaoAvaliadorRepository;
    @Mock CatalogoService catalogoService;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EdicaoService edicaoService;
    @Mock AuditoriaService auditoriaService;

    EdicaoAvaliadorService service;
    Comite comite;
    Campus campus;

    @BeforeEach
    void setUp() {
        service = new EdicaoAvaliadorService(edicaoAvaliadorRepository, catalogoService, usuarioRepository, edicaoService, auditoriaService);
        comite = TestData.comite(1L);
        campus = TestData.campus(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(TestData.avaliador(1L)));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(edicaoService.buscar(10L)).thenReturn(TestData.edicao(10L, EdicaoStatus.ATIVA, 3));
        when(edicaoAvaliadorRepository.findByAvaliadorIdAndStatus(1L, EdicaoAvaliadorStatus.ATIVO)).thenReturn(List.of());
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(10L, 1L)).thenReturn(Optional.empty());
        when(edicaoAvaliadorRepository.save(any(EdicaoAvaliador.class))).thenAnswer(i -> i.getArgument(0));
        when(catalogoService.resolverComite("Comitê de Tecnologia")).thenReturn(comite);
        when(catalogoService.resolverCampus("Campus Bacanga")).thenReturn(campus);
        // edicaoService é mock: reproduz aqui o comportamento real de garantirEditavel.
        doAnswer(inv -> {
            Edicao e = inv.getArgument(0);
            if (e.getStatus() == EdicaoStatus.ENCERRADA) {
                throw new RegraNegocioException("Edição encerrada é somente leitura");
            }
            return null;
        }).when(edicaoService).garantirEditavel(any());
    }

    private VincularAvaliadorCommand comando(Long edicaoId, Integer limite) {
        return new VincularAvaliadorCommand(edicaoId, "Comitê de Tecnologia", "Campus Bacanga", limite);
    }

    @Test
    void vincularCriaParticipacaoAtivaComComiteECampus() {
        EdicaoAvaliador participacao = service.vincular(1L, comando(10L, null), 9L);

        assertThat(participacao.getStatus()).isEqualTo(EdicaoAvaliadorStatus.ATIVO);
        assertThat(participacao.getEdicao().getId()).isEqualTo(10L);
        assertThat(participacao.getAvaliador().getId()).isEqualTo(1L);
        assertThat(participacao.getAvaliador().getComite()).isSameAs(comite);
        assertThat(participacao.getCampus()).isSameAs(campus);
    }

    @Test
    void vincularLiberaParticipacaoAtivaEmOutraEdicao() {
        Edicao outraEdicao = TestData.edicao(5L, EdicaoStatus.ENCERRADA, 3);
        EdicaoAvaliador antiga = new EdicaoAvaliador();
        antiga.setId(100L);
        antiga.setEdicao(outraEdicao);
        antiga.setAvaliador(TestData.avaliador(1L));
        antiga.setStatus(EdicaoAvaliadorStatus.ATIVO);
        when(edicaoAvaliadorRepository.findByAvaliadorIdAndStatus(1L, EdicaoAvaliadorStatus.ATIVO))
            .thenReturn(List.of(antiga));

        service.vincular(1L, comando(10L, null), 9L);

        assertThat(antiga.getStatus()).isEqualTo(EdicaoAvaliadorStatus.INATIVO);
    }

    @Test
    void vincularNaoAlteraParticipacaoNaMesmaEdicao() {
        Edicao edicao10 = TestData.edicao(10L, EdicaoStatus.ATIVA, 3);
        EdicaoAvaliador mesma = new EdicaoAvaliador();
        mesma.setId(200L);
        mesma.setEdicao(edicao10);
        mesma.setAvaliador(TestData.avaliador(1L));
        mesma.setStatus(EdicaoAvaliadorStatus.ATIVO);
        when(edicaoAvaliadorRepository.findByAvaliadorIdAndStatus(1L, EdicaoAvaliadorStatus.ATIVO))
            .thenReturn(List.of(mesma));
        when(edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(10L, 1L)).thenReturn(Optional.of(mesma));

        service.vincular(1L, comando(10L, 5), 9L);

        assertThat(mesma.getStatus()).isEqualTo(EdicaoAvaliadorStatus.ATIVO);
        assertThat(mesma.getLimiteMaxProjetos()).isEqualTo(5);
    }

    @Test
    void vincularRejeitaUsuarioQueNaoEhAvaliador() {
        Usuario admin = new Usuario();
        admin.setId(2L);
        admin.setTipo(UsuarioTipo.ADMIN);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.vincular(2L, comando(10L, null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("avaliador");
    }

    @Test
    void vincularAvaliadorInexistenteLancaNaoEncontrada() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.vincular(99L, comando(10L, null), 9L))
            .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void vincularRejeitaEdicaoEncerrada() {
        when(edicaoService.buscar(10L)).thenReturn(TestData.edicao(10L, EdicaoStatus.ENCERRADA, 3));

        assertThatThrownBy(() -> service.vincular(1L, comando(10L, null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("encerrada");
    }

    @Test
    void vincularSemComiteResolvidoEhRejeitado() {
        when(catalogoService.resolverComite("Comitê de Tecnologia")).thenReturn(null);

        assertThatThrownBy(() -> service.vincular(1L, comando(10L, null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("comitê");
    }

    @Test
    void vincularSemCampusResolvidoEhRejeitado() {
        when(catalogoService.resolverCampus("Campus Bacanga")).thenReturn(null);

        assertThatThrownBy(() -> service.vincular(1L, comando(10L, null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("campus");
    }

    @Test
    void vincularGravaAuditoria() {
        service.vincular(1L, comando(10L, null), 9L);

        verify(auditoriaService).registrar(eq(9L), eq("AVALIADOR_VINCULADO_EDICAO"), eq("EdicaoAvaliador"), any(), any());
    }
}
