package com.avaliacao.sistema.service;

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
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AtribuicaoService {

    private final ProjetoRepository projetoRepository;
    private final AtribuicaoRepository atribuicaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final EdicaoAvaliadorRepository edicaoAvaliadorRepository;
    private final ProjetoOrientadorRepository projetoOrientadorRepository;
    private final ProjetoDiscenteRepository projetoDiscenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EdicaoService edicaoService;
    private final AuditoriaService auditoriaService;
    private final Random random;
    private final Clock clock;

    // Se o pool não bastar, atribui o que der e devolve o que falta em faltantes(): nunca falha em silêncio
    @Transactional
    public ResultadoAtribuicao atribuirProjeto(Long projetoId) {
        Projeto projeto = buscarProjeto(projetoId);
        Edicao edicao = projeto.getEdicao();
        if (edicao.getStatus() != EdicaoStatus.ATIVA) {
            throw new RegraNegocioException("Só é possível atribuir avaliadores em uma edição ativa");
        }
        List<EdicaoAvaliador> pool = edicaoAvaliadorRepository.findByEdicaoIdAndStatus(edicao.getId(), EdicaoAvaliadorStatus.ATIVO);
        return atribuirProjeto(projeto, edicao, pool);
    }

    @Transactional
    public List<ResultadoAtribuicao> atribuirEdicaoAtiva() {
        Edicao edicao = edicaoService.buscarAtiva();
        // O pool de avaliadores da edição é o mesmo pra todos os projetos sorteados nesta chamada;
        // buscar uma vez só em vez de a cada projeto evita N consultas idênticas.
        List<EdicaoAvaliador> pool = edicaoAvaliadorRepository.findByEdicaoIdAndStatus(edicao.getId(), EdicaoAvaliadorStatus.ATIVO);
        return projetoRepository.findByEdicaoIdAndAtivoTrue(edicao.getId()).stream()
            .map(p -> atribuirProjeto(p, edicao, pool))
            .toList();
    }

    private ResultadoAtribuicao atribuirProjeto(Projeto projeto, Edicao edicao, List<EdicaoAvaliador> pool) {
        Long projetoId = projeto.getId();
        List<Atribuicao> ativas = atribuicaoRepository.findByProjetoIdAndStatus(projetoId, AtribuicaoStatus.ATIVA);
        int necessarios = edicao.getMinAvaliadoresPorProjeto() - ativas.size();
        if (necessarios <= 0) {
            return new ResultadoAtribuicao(projetoId, ativas.size(), 0, 0, List.of(), List.of());
        }

        List<String> avisos = new ArrayList<>();
        if (projeto.getComite() == null) {
            avisos.add("Projeto sem comitê: não há como filtrar avaliadores");
            registrarPendencia(projetoId, necessarios, avisos);
            return new ResultadoAtribuicao(projetoId, ativas.size(), 0, necessarios, List.of(), avisos);
        }
        if (projeto.getCampus() == null) {
            avisos.add("Projeto sem campus: não há como filtrar avaliadores");
            registrarPendencia(projetoId, necessarios, avisos);
            return new ResultadoAtribuicao(projetoId, ativas.size(), 0, necessarios, List.of(), avisos);
        }

        Set<Long> jaAtribuidos = new HashSet<>();
        ativas.forEach(a -> jaAtribuidos.add(a.getAvaliador().getId()));
        Set<Long> pessoasImpedidas = pessoasImpedidas(projetoId);

        List<Usuario> elegiveis = new ArrayList<>();
        for (EdicaoAvaliador participacao : pool) {
            Usuario avaliador = participacao.getAvaliador();
            if (jaAtribuidos.contains(avaliador.getId())) {
                continue;
            }
            if (!mesmoComite(avaliador.getComite(), projeto.getComite()) || !mesmoCampus(participacao.getCampus(), projeto.getCampus())) {
                continue;
            }
            if (pessoasImpedidas.contains(avaliador.getPessoa().getId())) {
                avisos.add("Avaliador " + avaliador.getId() + " excluído por conflito de interesse");
                continue;
            }
            if (atingiuLimiteDeCarga(participacao, edicao)) {
                continue;
            }
            elegiveis.add(avaliador);
        }

        Collections.shuffle(elegiveis, random);
        List<Usuario> sorteados = elegiveis.subList(0, Math.min(necessarios, elegiveis.size()));

        List<Long> ids = new ArrayList<>();
        for (Usuario avaliador : sorteados) {
            Atribuicao atribuicao = criarAtribuicao(projeto, avaliador, AtribuicaoOrigem.AUTOMATICA, null, null);
            ids.add(avaliador.getId());
            auditoriaService.registrar(null, "ATRIBUICAO_AUTOMATICA", "Atribuicao", atribuicao.getId(),
                "projeto=" + projetoId + ", avaliador=" + avaliador.getId());
        }

        int faltantes = necessarios - sorteados.size();
        if (faltantes > 0) {
            avisos.add("Pool insuficiente: faltam " + faltantes + " avaliador(es)");
            registrarPendencia(projetoId, faltantes, avisos);
        }
        return new ResultadoAtribuicao(projetoId, ativas.size(), sorteados.size(), faltantes, ids, avisos);
    }

    /** Exceção à via automática: cancela a atribuição antiga e cria uma manual, sempre com motivo e log. */
    @Transactional
    public Atribuicao reatribuir(Long atribuicaoId, Long novoAvaliadorId, String motivo, Long adminId) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da reatribuição é obrigatório");
        }
        Atribuicao antiga = atribuicaoRepository.findById(atribuicaoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Atribuição", atribuicaoId));
        if (antiga.getStatus() != AtribuicaoStatus.ATIVA) {
            throw new RegraNegocioException("Só uma atribuição ativa pode ser reatribuída");
        }
        Avaliacao avaliacao = avaliacaoRepository.findByAtribuicaoId(atribuicaoId).orElse(null);
        if (avaliacao != null && avaliacao.getStatus() == AvaliacaoStatus.ENVIADA) {
            throw new RegraNegocioException("A avaliação já foi enviada; reabra-a antes de reatribuir");
        }

        Projeto projeto = antiga.getProjeto();
        Edicao edicao = projeto.getEdicao();
        Usuario novo = usuarioRepository.findById(novoAvaliadorId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliador", novoAvaliadorId));

        EdicaoAvaliador participacaoNovo = edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(edicao.getId(), novoAvaliadorId)
            .filter(p -> p.getStatus() == EdicaoAvaliadorStatus.ATIVO)
            .orElseThrow(() -> new RegraNegocioException("O avaliador não participa desta edição"));
        // Mesma regra de elegibilidade do sorteio automático: também vale pra reatribuição manual
        if (!mesmoComite(novo.getComite(), projeto.getComite())) {
            throw new RegraNegocioException("O avaliador não pertence ao comitê deste projeto");
        }
        if (!mesmoCampus(participacaoNovo.getCampus(), projeto.getCampus())) {
            throw new RegraNegocioException("O avaliador não atua no campus deste projeto");
        }
        if (pessoasImpedidas(projeto.getId()).contains(novo.getPessoa().getId())) {
            throw new RegraNegocioException("Conflito de interesse: o avaliador é autor ou orientador do projeto");
        }
        boolean jaAtribuido = atribuicaoRepository.findByProjetoIdAndStatus(projeto.getId(), AtribuicaoStatus.ATIVA)
            .stream().anyMatch(a -> a.getAvaliador().getId().equals(novoAvaliadorId));
        if (jaAtribuido) {
            throw new RegraNegocioException("O avaliador já está atribuído a este projeto");
        }

        antiga.setStatus(AtribuicaoStatus.CANCELADA);
        atribuicaoRepository.save(antiga);
        Atribuicao nova = criarAtribuicao(projeto, novo, AtribuicaoOrigem.MANUAL, motivo, adminId);

        auditoriaService.registrar(adminId, "REATRIBUICAO_MANUAL", "Atribuicao", nova.getId(),
            "de=" + antiga.getAvaliador().getId() + " para=" + novoAvaliadorId + " motivo=" + motivo);
        return nova;
    }

    private Atribuicao criarAtribuicao(Projeto projeto, Usuario avaliador, AtribuicaoOrigem origem,
                                       String motivo, Long adminId) {
        Atribuicao atribuicao = new Atribuicao();
        atribuicao.setProjeto(projeto);
        atribuicao.setAvaliador(avaliador);
        atribuicao.setStatus(AtribuicaoStatus.ATIVA);
        atribuicao.setOrigem(origem);
        atribuicao.setMotivo(motivo);
        atribuicao.setCriadoPor(adminId == null ? null : usuarioRepository.getReferenceById(adminId));
        atribuicao.setCreatedAt(Instant.now(clock));
        return atribuicaoRepository.save(atribuicao);
    }

    private Projeto buscarProjeto(Long projetoId) {
        return projetoRepository.findById(projetoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Projeto", projetoId));
    }

    private Set<Long> pessoasImpedidas(Long projetoId) {
        Set<Long> ids = new HashSet<>();
        projetoOrientadorRepository.findByProjetoId(projetoId).forEach(o -> ids.add(o.getPessoa().getId()));
        projetoDiscenteRepository.findByProjetoId(projetoId).forEach(d -> ids.add(d.getPessoa().getId()));
        return ids;
    }

    private boolean atingiuLimiteDeCarga(EdicaoAvaliador participacao, Edicao edicao) {
        Integer limite = participacao.getLimiteMaxProjetos() != null
            ? participacao.getLimiteMaxProjetos()
            : edicao.getLimiteMaxProjetosPorAvaliador();
        if (limite == null) {
            return false;
        }
        int atual = atribuicaoRepository.countByAvaliadorIdAndStatusAndProjetoEdicaoId(
            participacao.getAvaliador().getId(), AtribuicaoStatus.ATIVA, edicao.getId());
        return atual >= limite;
    }

    private boolean mesmoComite(Comite doAvaliador, Comite doProjeto) {
        return doAvaliador != null && doProjeto != null && doAvaliador.getId().equals(doProjeto.getId());
    }

    private boolean mesmoCampus(Campus doAvaliador, Campus doProjeto) {
        return doAvaliador != null && doProjeto != null && doAvaliador.getId().equals(doProjeto.getId());
    }

    private void registrarPendencia(Long projetoId, int faltantes, List<String> avisos) {
        auditoriaService.registrar(null, "ATRIBUICAO_INCOMPLETA", "Projeto", projetoId,
            "faltam " + faltantes + " avaliador(es); " + String.join("; ", avisos));
    }
}
