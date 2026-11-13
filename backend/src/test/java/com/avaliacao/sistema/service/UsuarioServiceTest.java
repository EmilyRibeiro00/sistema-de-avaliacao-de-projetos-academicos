package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Instituicao;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.PessoaRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.CadastroAvaliadorCommand;
import com.avaliacao.sistema.service.dto.EditarAvaliadorCommand;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UsuarioServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-01T00:00:00Z");

    @Mock UsuarioRepository usuarioRepository;
    @Mock PessoaRepository pessoaRepository;
    @Mock CatalogoService catalogoService;
    @Mock AuditoriaService auditoriaService;
    @Mock EmailService emailService;
    @Mock PasswordEncoder passwordEncoder;

    UsuarioService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        service = new UsuarioService(usuarioRepository, pessoaRepository, catalogoService, auditoriaService, emailService, clock, passwordEncoder);
        ReflectionTestUtils.setField(service, "senhaPadraoAvaliador", "Trocar@123");
        when(passwordEncoder.encode(any())).thenReturn("hash-bcrypt");
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findByPessoaEmail(any())).thenReturn(Optional.empty());
        when(usuarioRepository.findByPessoaCpf(any())).thenReturn(Optional.empty());
    }

    private CadastroAvaliadorCommand comando(String instituicaoNome) {
        return new CadastroAvaliadorCommand("Maria Avaliadora", "maria@instituicao.edu.br", "11122233344",
            null, null, instituicaoNome);
    }

    @Test
    void cadastrarCriaUsuarioAvaliadorComSenhaPadraoEPendenteDeTroca() {
        Usuario criado = service.cadastrarAvaliador(comando(null), 1L);

        assertThat(criado.getTipo()).isEqualTo(UsuarioTipo.AVALIADOR);
        assertThat(criado.getStatusAtivacao()).isEqualTo(UsuarioStatusAtivacao.PENDENTE_ATIVACAO);
        assertThat(criado.getSenhaHash()).isEqualTo("hash-bcrypt");
        assertThat(criado.getPrecisaTrocarSenha()).isTrue();
        assertThat(criado.getPessoa().getNome()).isEqualTo("Maria Avaliadora");
    }

    @Test
    void cadastrarEnviaASenhaPadraoPorEmail() {
        service.cadastrarAvaliador(comando(null), 1L);

        verify(emailService).enviarCredenciaisIniciais(eq("maria@instituicao.edu.br"), eq("Maria Avaliadora"), eq("Trocar@123"));
    }

    @Test
    void cadastrarGravaAuditoria() {
        Usuario criado = service.cadastrarAvaliador(comando(null), 9L);

        verify(auditoriaService).registrar(eq(9L), eq("AVALIADOR_CADASTRADO"), eq("Usuario"), any(), eq("maria@instituicao.edu.br"));
        assertThat(criado).isNotNull();
    }

    @Test
    void cadastrarComEmailJaExistenteLancaRegraDeNegocio() {
        when(usuarioRepository.findByPessoaEmail("maria@instituicao.edu.br"))
            .thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.cadastrarAvaliador(comando(null), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("e-mail");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastrarComCpfJaExistenteLancaRegraDeNegocio() {
        when(usuarioRepository.findByPessoaCpf("11122233344")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> service.cadastrarAvaliador(comando(null), 1L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("CPF");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastrarSemInstituicaoNaoConsultaOCatalogoAlemDoNullEsperado() {
        when(catalogoService.resolverInstituicao(null)).thenReturn(null);

        Usuario criado = service.cadastrarAvaliador(comando(null), 1L);

        assertThat(criado.getPessoa().getInstituicao()).isNull();
    }

    @Test
    void cadastrarResolveAInstituicaoViaCatalogoService() {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(5L);
        instituicao.setNome("UFXX");
        when(catalogoService.resolverInstituicao("UFXX")).thenReturn(instituicao);

        Usuario criado = service.cadastrarAvaliador(comando("UFXX"), 1L);

        assertThat(criado.getPessoa().getInstituicao()).isSameAs(instituicao);
    }

    private EditarAvaliadorCommand editarComando(String comiteNome) {
        return new EditarAvaliadorCommand("Maria Editada", "maria.editada@instituicao.edu.br", "55566677788",
            null, null, null, comiteNome);
    }

    @Test
    void listarDevolveSoAvaliadores() {
        service.listar();

        verify(usuarioRepository).findByTipo(UsuarioTipo.AVALIADOR);
    }

    @Test
    void atualizarAlteraDadosDaPessoaEGravaAuditoria() {
        Usuario avaliador = TestData.avaliador(3L);
        when(usuarioRepository.findByIdComDetalhes(3L)).thenReturn(Optional.of(avaliador));

        Usuario atualizado = service.atualizar(3L, editarComando(null), 9L);

        assertThat(atualizado.getPessoa().getNome()).isEqualTo("Maria Editada");
        assertThat(atualizado.getPessoa().getEmail()).isEqualTo("maria.editada@instituicao.edu.br");
        verify(auditoriaService).registrar(eq(9L), eq("AVALIADOR_ATUALIZADO"), eq("Usuario"), eq(3L), any());
    }

    @Test
    void atualizarComComiteInformadoResolveViaCatalogoService() {
        Usuario avaliador = TestData.avaliador(3L);
        when(usuarioRepository.findByIdComDetalhes(3L)).thenReturn(Optional.of(avaliador));
        Comite comite = TestData.comite(4L);
        when(catalogoService.resolverComite("Comitê de Tecnologia")).thenReturn(comite);

        Usuario atualizado = service.atualizar(3L, editarComando("Comitê de Tecnologia"), 9L);

        assertThat(atualizado.getComite()).isSameAs(comite);
    }

    @Test
    void atualizarSemComiteInformadoNaoAlteraOComiteExistente() {
        Usuario avaliador = TestData.avaliador(3L);
        Comite comiteAtual = TestData.comite(1L);
        avaliador.setComite(comiteAtual);
        when(usuarioRepository.findByIdComDetalhes(3L)).thenReturn(Optional.of(avaliador));

        Usuario atualizado = service.atualizar(3L, editarComando(null), 9L);

        assertThat(atualizado.getComite()).isSameAs(comiteAtual);
        verify(catalogoService, never()).resolverComite(any());
    }

    @Test
    void atualizarComEmailDeOutroUsuarioEhRejeitado() {
        Usuario avaliador = TestData.avaliador(3L);
        when(usuarioRepository.findByIdComDetalhes(3L)).thenReturn(Optional.of(avaliador));
        Usuario outro = TestData.avaliador(4L);
        when(usuarioRepository.findByPessoaEmail("maria.editada@instituicao.edu.br")).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> service.atualizar(3L, editarComando(null), 9L))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessageContaining("e-mail");
    }

    @Test
    void atualizarAvaliadorInexistenteLancaNaoEncontrado() {
        when(usuarioRepository.findByIdComDetalhes(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizar(99L, editarComando(null), 9L))
            .isInstanceOf(EntidadeNaoEncontradaException.class);
    }

    @Test
    void removerMarcaStatusInativoEGravaAuditoria() {
        Usuario avaliador = TestData.avaliador(3L);
        when(usuarioRepository.findByIdComDetalhes(3L)).thenReturn(Optional.of(avaliador));

        service.remover(3L, 9L);

        assertThat(avaliador.getStatusAtivacao()).isEqualTo(UsuarioStatusAtivacao.INATIVO);
        verify(auditoriaService).registrar(eq(9L), eq("AVALIADOR_REMOVIDO"), eq("Usuario"), eq(3L), any());
    }
}
