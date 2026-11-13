package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.ProjetoDiscente;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoDiscentePapel;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.PessoaRepository;
import com.avaliacao.sistema.repository.ProjetoDiscenteRepository;
import com.avaliacao.sistema.repository.ProjetoRepository;
import com.avaliacao.sistema.service.dto.CadastroProjetoCommand;
import com.avaliacao.sistema.service.dto.EditarProjetoCommand;
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
class ProjetoServiceTest {

    @Mock ProjetoRepository projetoRepository;
    @Mock ProjetoDiscenteRepository projetoDiscenteRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock CatalogoService catalogoService;
    @Mock EdicaoService edicaoService;
    @Mock AuditoriaService auditoriaService;

    ProjetoService service;

    @BeforeEach
    void setUp() {
        service = new ProjetoService(projetoRepository, projetoDiscenteRepository, pessoaRepository,
            catalogoService, edicaoService, auditoriaService);
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(i -> i.getArgument(0));
        when(projetoDiscenteRepository.save(any(ProjetoDiscente.class))).thenAnswer(i -> i.getArgument(0));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(i -> i.getArgument(0));
        when(edicaoService.buscar(1L)).thenReturn(TestData.edicao(1L, EdicaoStatus.ATIVA, 3));
        when(projetoRepository.findBySigaaIdAndEdicaoId(any(), any())).thenReturn(Optional.empty());
        // edicaoService é mock: reproduz aqui o comportamento real de garantirEditavel.
        doAnswer(inv -> {
            Edicao e = inv.getArgument(0);
            if (e.getStatus() == EdicaoStatus.ENCERRADA) {
                throw new RegraNegocioException("Edição encerrada é somente leitura");
            }
            return null;
        }).when(edicaoService).garantirEditavel(any());
    }

    private CadastroProjetoCommand comando(String sigaaId, String comiteNome, String autorCpf) {
        return new CadastroProjetoCommand(1L, sigaaId, "Projeto de teste", "Conteúdo técnico",
            comiteNome, null, null, "Autor Teste", "autor@teste.br", autorCpf);
    }

    @Test
    void cadastrarCriaProjetoSubmetidoComAutor() {
        Projeto criado = service.cadastrarManualmente(comando("SIGAA-1", null, null), 1L);

        assertThat(criado.getStatus()).isEqualTo(ProjetoStatus.SUBMETIDO);
        assertThat(criado.getSigaaId()).isEqualTo("SIGAA-1");
        assertThat(criado.getTitulo()).isEqualTo("Projeto de teste");

        var captor = org.mockito.ArgumentCaptor.forClass(ProjetoDiscente.class);
        verify(projetoDiscenteRepository).save(captor.capture());
        assertThat(captor.getValue().getPapel()).isEqualTo(ProjetoDiscentePapel.AUTOR);
        assertThat(captor.getValue().getPessoa().getNome()).isEqualTo("Autor Teste");
    }

    @Test
    void cadastrarGeraSigaaIdQuandoNaoInformado() {
        Projeto criado = service.cadastrarManualmente(comando(null, null, null), 1L);

        assertThat(criado.getSigaaId()).startsWith("MANUAL-");
    }

    @Test
    void cadastrarRejeitaSigaaIdDuplicadoNaMesmaEdicao() {
        when(projetoRepository.findBySigaaIdAndEdicaoId("SIGAA-1", 1L))
            .thenReturn(Optional.of(new Projeto()));

        assertThatThrownBy(() -> service.cadastrarManualmente(comando("SIGAA-1", null, null), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("identificador");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void cadastrarRejeitaEdicaoEncerrada() {
        when(edicaoService.buscar(1L)).thenReturn(TestData.edicao(1L, EdicaoStatus.ENCERRADA, 3));

        assertThatThrownBy(() -> service.cadastrarManualmente(comando("SIGAA-1", null, null), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("encerrada");
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void cadastrarResolveOComiteViaCatalogoService() {
        Comite comite = TestData.comite(9L);
        when(catalogoService.resolverComite("Computação")).thenReturn(comite);

        Projeto criado = service.cadastrarManualmente(comando("SIGAA-1", "Computação", null), 1L);

        assertThat(criado.getComite()).isSameAs(comite);
    }

    @Test
    void cadastrarSemComiteNaoConsultaOCatalogoAlemDoNullEsperado() {
        when(catalogoService.resolverComite(null)).thenReturn(null);

        Projeto criado = service.cadastrarManualmente(comando("SIGAA-1", null, null), 1L);

        assertThat(criado.getComite()).isNull();
    }

    @Test
    void cadastrarResolveOCampusViaCatalogoService() {
        Campus campus = TestData.campus(9L);
        CadastroProjetoCommand cmd = new CadastroProjetoCommand(1L, "SIGAA-1", "Projeto de teste", "Conteúdo técnico",
            null, "Campus Bacanga", null, "Autor Teste", "autor@teste.br", null);
        when(catalogoService.resolverCampus("Campus Bacanga")).thenReturn(campus);

        Projeto criado = service.cadastrarManualmente(cmd, 1L);

        assertThat(criado.getCampus()).isSameAs(campus);
    }

    @Test
    void cadastrarComCpfDoAutorJaExistenteReaproveitaAPessoa() {
        Pessoa existente = new Pessoa();
        existente.setId(42L);
        existente.setNome("Autor Teste");
        when(pessoaRepository.findByCpf("11122233344")).thenReturn(Optional.of(existente));

        service.cadastrarManualmente(comando("SIGAA-1", null, "11122233344"), 1L);

        verify(pessoaRepository, never()).save(any());
    }

    @Test
    void cadastrarGravaAuditoria() {
        Edicao edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        when(edicaoService.buscar(1L)).thenReturn(edicao);

        Projeto criado = service.cadastrarManualmente(comando("SIGAA-1", null, null), 7L);

        verify(auditoriaService).registrar(eq(7L), eq("PROJETO_CADASTRADO_MANUALMENTE"), eq("Projeto"), any(), eq(criado.getTitulo()));
    }

    @Test
    void atualizarAlteraCamposEResolveComiteECampusEGravaAuditoria() {
        Edicao edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        Projeto projeto = TestData.projeto(5L, edicao, null, null);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(projeto));
        Comite comite = TestData.comite(2L);
        Campus campus = TestData.campus(3L);
        when(catalogoService.resolverComite("Comitê de Tecnologia")).thenReturn(comite);
        when(catalogoService.resolverCampus("Campus Bacanga")).thenReturn(campus);

        Projeto atualizado = service.atualizar(5L,
            new EditarProjetoCommand("Novo título", "Novo conteúdo", "Comitê de Tecnologia", "Campus Bacanga", null), 9L);

        assertThat(atualizado.getTitulo()).isEqualTo("Novo título");
        assertThat(atualizado.getComite()).isSameAs(comite);
        assertThat(atualizado.getCampus()).isSameAs(campus);
        verify(auditoriaService).registrar(eq(9L), eq("PROJETO_ATUALIZADO"), eq("Projeto"), eq(5L), eq("Novo título"));
    }

    @Test
    void atualizarProjetoDeEdicaoEncerradaEhRejeitado() {
        Edicao edicao = TestData.edicao(1L, EdicaoStatus.ENCERRADA, 3);
        Projeto projeto = TestData.projeto(5L, edicao, null, null);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(projeto));

        assertThatThrownBy(() -> service.atualizar(5L, new EditarProjetoCommand("X", "Y", null, null, null), 9L))
            .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void removerMarcaInativoEmVezDeApagarEGravaAuditoria() {
        Edicao edicao = TestData.edicao(1L, EdicaoStatus.ATIVA, 3);
        Projeto projeto = TestData.projeto(5L, edicao, null, null);
        when(projetoRepository.findById(5L)).thenReturn(Optional.of(projeto));

        service.remover(5L, 9L);

        assertThat(projeto.getAtivo()).isFalse();
        verify(auditoriaService).registrar(eq(9L), eq("PROJETO_REMOVIDO"), eq("Projeto"), eq(5L), any());
    }

    @Test
    void listarSemEdicaoIdDevolveTodosOsAtivos() {
        service.listar(null);

        verify(projetoRepository).findByAtivoTrue();
        verify(projetoRepository, never()).findByEdicaoIdAndAtivoTrue(any());
    }

    @Test
    void listarComEdicaoIdFiltraPelaEdicao() {
        when(projetoRepository.findByEdicaoIdAndAtivoTrue(1L)).thenReturn(List.of());

        service.listar(1L);

        verify(projetoRepository).findByEdicaoIdAndAtivoTrue(1L);
        verify(projetoRepository, never()).findByAtivoTrue();
    }
}
