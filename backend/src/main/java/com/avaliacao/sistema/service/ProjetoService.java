package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.ProjetoDiscente;
import com.avaliacao.sistema.entity.enums.ProjetoDiscentePapel;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.PessoaRepository;
import com.avaliacao.sistema.repository.ProjetoDiscenteRepository;
import com.avaliacao.sistema.repository.ProjetoRepository;
import com.avaliacao.sistema.service.dto.CadastroProjetoCommand;
import com.avaliacao.sistema.service.dto.EditarProjetoCommand;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoDiscenteRepository projetoDiscenteRepository;
    private final PessoaRepository pessoaRepository;
    private final CatalogoService catalogoService;
    private final EdicaoService edicaoService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Projeto> listar(Long edicaoId) {
        return edicaoId == null ? projetoRepository.findByAtivoTrue() : projetoRepository.findByEdicaoIdAndAtivoTrue(edicaoId);
    }

    /** Cadastro manual (não é a importação do Sigaa — essa ainda não existe): o admin digita os dados na hora. */
    @Transactional
    public Projeto cadastrarManualmente(CadastroProjetoCommand cmd, Long adminId) {
        var edicao = edicaoService.buscar(cmd.edicaoId());
        edicaoService.garantirEditavel(edicao);

        String sigaaId = (cmd.sigaaId() == null || cmd.sigaaId().isBlank())
            ? "MANUAL-" + UUID.randomUUID()
            : cmd.sigaaId().trim();
        if (projetoRepository.findBySigaaIdAndEdicaoId(sigaaId, edicao.getId()).isPresent()) {
            throw new RegraNegocioException("Já existe um projeto com este identificador nesta edição");
        }

        Projeto projeto = new Projeto();
        projeto.setEdicao(edicao);
        projeto.setSigaaId(sigaaId);
        projeto.setTitulo(cmd.titulo());
        projeto.setConteudoTecnico(cmd.conteudoTecnico());
        projeto.setComite(catalogoService.resolverComite(cmd.comiteNome()));
        projeto.setCampus(catalogoService.resolverCampus(cmd.campusNome()));
        projeto.setInstituicao(catalogoService.resolverInstituicao(cmd.instituicaoNome()));
        projeto.setStatus(ProjetoStatus.SUBMETIDO);
        Projeto salvo = projetoRepository.save(projeto);

        Pessoa autor = buscarOuCriarAutor(cmd.autorNome(), cmd.autorEmail(), cmd.autorCpf());
        ProjetoDiscente discente = new ProjetoDiscente();
        discente.setProjeto(salvo);
        discente.setPessoa(autor);
        discente.setPapel(ProjetoDiscentePapel.AUTOR);
        projetoDiscenteRepository.save(discente);

        auditoriaService.registrar(adminId, "PROJETO_CADASTRADO_MANUALMENTE", "Projeto", salvo.getId(), salvo.getTitulo());
        return salvo;
    }

    @Transactional
    public Projeto atualizar(Long projetoId, EditarProjetoCommand cmd, Long adminId) {
        Projeto projeto = buscar(projetoId);
        edicaoService.garantirEditavel(projeto.getEdicao());

        projeto.setTitulo(cmd.titulo());
        projeto.setConteudoTecnico(cmd.conteudoTecnico());
        projeto.setComite(catalogoService.resolverComite(cmd.comiteNome()));
        projeto.setCampus(catalogoService.resolverCampus(cmd.campusNome()));
        projeto.setInstituicao(catalogoService.resolverInstituicao(cmd.instituicaoNome()));
        Projeto salvo = projetoRepository.save(projeto);

        auditoriaService.registrar(adminId, "PROJETO_ATUALIZADO", "Projeto", salvo.getId(), salvo.getTitulo());
        return salvo;
    }

    /** Remoção lógica: marca ativo=false em vez de apagar, para preservar atribuições/avaliações já ligadas ao projeto. */
    @Transactional
    public void remover(Long projetoId, Long adminId) {
        Projeto projeto = buscar(projetoId);
        edicaoService.garantirEditavel(projeto.getEdicao());
        projeto.setAtivo(false);
        projetoRepository.save(projeto);
        auditoriaService.registrar(adminId, "PROJETO_REMOVIDO", "Projeto", projetoId, projeto.getTitulo());
    }

    private Projeto buscar(Long projetoId) {
        return projetoRepository.findById(projetoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Projeto", projetoId));
    }

    // Reaproveita a Pessoa pelo CPF se já existir (ex.: mesmo autor em outro projeto); senão cria uma nova.
    private Pessoa buscarOuCriarAutor(String nome, String email, String cpf) {
        if (cpf != null && !cpf.isBlank()) {
            var existente = pessoaRepository.findByCpf(cpf.trim());
            if (existente.isPresent()) {
                return existente.get();
            }
        }
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(nome);
        pessoa.setEmail(email == null || email.isBlank() ? null : email.trim());
        pessoa.setCpf(cpf == null || cpf.isBlank() ? null : cpf.trim());
        return pessoaRepository.save(pessoa);
    }
}
