package com.avaliacao.sistema.service;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final CatalogoService catalogoService;
    private final AuditoriaService auditoriaService;
    private final EmailService emailService;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.auth.senha-padrao-avaliador}")
    private String senhaPadraoAvaliador;

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findByTipo(UsuarioTipo.AVALIADOR);
    }

    public String getSenhaPadraoAvaliador() {
        return senhaPadraoAvaliador;
    }

    /**
     * Cria o avaliador já com a senha padrão (ele loga direto, sem link de ativação) e marcado para
     * trocá-la no primeiro acesso. status_ativacao permanece PENDENTE_ATIVACAO (não é mais usado pra
     * controlar login — ver UsuarioPrincipal.isEnabled — só fica reservado pra quando o comitê for
     * vinculado; chk_usuario_avaliador_comite impede ATIVO sem comitê).
     */
    @Transactional
    public Usuario cadastrarAvaliador(CadastroAvaliadorCommand cmd, Long adminId) {
        if (usuarioRepository.findByPessoaEmail(cmd.email()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com este e-mail");
        }
        if (usuarioRepository.findByPessoaCpf(cmd.cpf()).isPresent()) {
            throw new RegraNegocioException("Já existe um usuário com este CPF");
        }

        Pessoa pessoa = new Pessoa();
        pessoa.setNome(cmd.nome());
        pessoa.setEmail(cmd.email());
        pessoa.setCpf(cmd.cpf());
        pessoa.setMatricula(cmd.matricula());
        pessoa.setSiape(cmd.siape());
        pessoa.setInstituicao(catalogoService.resolverInstituicao(cmd.instituicaoNome()));
        Pessoa pessoaSalva = pessoaRepository.save(pessoa);

        Instant agora = Instant.now(clock);
        Usuario usuario = new Usuario();
        usuario.setPessoa(pessoaSalva);
        usuario.setTipo(UsuarioTipo.AVALIADOR);
        usuario.setStatusAtivacao(UsuarioStatusAtivacao.PENDENTE_ATIVACAO);
        usuario.setSenhaHash(passwordEncoder.encode(senhaPadraoAvaliador));
        usuario.setPrecisaTrocarSenha(true);
        usuario.setCreatedAt(agora);
        usuario.setUpdatedAt(agora);
        Usuario salvo = usuarioRepository.save(usuario);

        emailService.enviarCredenciaisIniciais(pessoaSalva.getEmail(), pessoaSalva.getNome(), senhaPadraoAvaliador);
        auditoriaService.registrar(adminId, "AVALIADOR_CADASTRADO", "Usuario", salvo.getId(), pessoaSalva.getEmail());
        return salvo;
    }

    @Transactional
    public Usuario atualizar(Long avaliadorId, EditarAvaliadorCommand cmd, Long adminId) {
        Usuario usuario = buscarAvaliador(avaliadorId);
        Pessoa pessoa = usuario.getPessoa();

        usuarioRepository.findByPessoaEmail(cmd.email())
            .filter(outro -> !outro.getId().equals(avaliadorId))
            .ifPresent(outro -> {
                throw new RegraNegocioException("Já existe um usuário com este e-mail");
            });
        usuarioRepository.findByPessoaCpf(cmd.cpf())
            .filter(outro -> !outro.getId().equals(avaliadorId))
            .ifPresent(outro -> {
                throw new RegraNegocioException("Já existe um usuário com este CPF");
            });

        pessoa.setNome(cmd.nome());
        pessoa.setEmail(cmd.email());
        pessoa.setCpf(cmd.cpf());
        pessoa.setMatricula(cmd.matricula());
        pessoa.setSiape(cmd.siape());
        pessoa.setInstituicao(catalogoService.resolverInstituicao(cmd.instituicaoNome()));
        pessoaRepository.save(pessoa);

        // Comitê é opcional aqui de propósito: só troca se vier preenchido, pra nunca zerar o comitê de
        // um avaliador já ATIVO sem querer (o banco rejeitaria — chk_usuario_avaliador_comite).
        if (cmd.comiteNome() != null && !cmd.comiteNome().isBlank()) {
            usuario.setComite(catalogoService.resolverComite(cmd.comiteNome()));
        }
        Usuario salvo = usuarioRepository.save(usuario);

        auditoriaService.registrar(adminId, "AVALIADOR_ATUALIZADO", "Usuario", salvo.getId(), pessoa.getEmail());
        return salvo;
    }

    /** Remoção lógica: reaproveita status_ativacao=INATIVO (já existia) em vez de apagar. */
    @Transactional
    public void remover(Long avaliadorId, Long adminId) {
        Usuario usuario = buscarAvaliador(avaliadorId);
        usuario.setStatusAtivacao(UsuarioStatusAtivacao.INATIVO);
        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrar(adminId, "AVALIADOR_REMOVIDO", "Usuario", avaliadorId, salvo.getPessoa().getEmail());
    }

    private Usuario buscarAvaliador(Long avaliadorId) {
        Usuario usuario = usuarioRepository.findByIdComDetalhes(avaliadorId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliador", avaliadorId));
        if (usuario.getTipo() != UsuarioTipo.AVALIADOR) {
            throw new RegraNegocioException("Usuário informado não é um avaliador");
        }
        return usuario;
    }
}
