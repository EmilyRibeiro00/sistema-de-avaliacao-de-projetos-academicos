package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Avaliacao;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.repository.AtribuicaoRepository;
import com.avaliacao.sistema.repository.AvaliacaoRepository;
import com.avaliacao.sistema.repository.ProjetoRepository;
import com.avaliacao.sistema.service.dto.NotaAvaliadorView;
import com.avaliacao.sistema.service.dto.ResultadoConsolidacao;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsolidacaoService {

    private final ProjetoRepository projetoRepository;
    private final AtribuicaoRepository atribuicaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    // Nota do projeto = média das notas finais enviadas; é calculada aqui, nunca gravada no projeto
    @Transactional(readOnly = true)
    public ResultadoConsolidacao consolidar(Long projetoId) {
        Projeto projeto = buscarProjeto(projetoId);
        return consolidar(projeto);
    }

    /** Recalcula e grava o status do projeto (EM_AVALIACAO ou AVALIADO) conforme os pareceres enviados. */
    @Transactional
    public ResultadoConsolidacao atualizarStatus(Long projetoId) {
        Projeto projeto = buscarProjeto(projetoId);
        ResultadoConsolidacao resultado = consolidar(projeto);
        ProjetoStatus novo = resultado.avaliado() ? ProjetoStatus.AVALIADO
            : resultado.avaliadoresAtribuidos() > 0 ? ProjetoStatus.EM_AVALIACAO : projeto.getStatus();
        if (novo != projeto.getStatus()) {
            projeto.setStatus(novo);
            projetoRepository.save(projeto);
        }
        return resultado;
    }

    private ResultadoConsolidacao consolidar(Projeto projeto) {
        Edicao edicao = projeto.getEdicao();
        List<Atribuicao> ativas = atribuicaoRepository.findByProjetoIdAndStatus(projeto.getId(), AtribuicaoStatus.ATIVA);

        List<Long> atribuicaoIds = ativas.stream().map(Atribuicao::getId).toList();
        Map<Long, Avaliacao> avaliacoesPorAtribuicao = atribuicaoIds.isEmpty() ? Map.of()
            : avaliacaoRepository.findByAtribuicaoIdIn(atribuicaoIds).stream()
                .collect(Collectors.toMap(a -> a.getAtribuicao().getId(), a -> a));

        List<NotaAvaliadorView> notas = new ArrayList<>();
        for (Atribuicao atribuicao : ativas) {
            Avaliacao avaliacao = avaliacoesPorAtribuicao.get(atribuicao.getId());
            if (avaliacao != null && avaliacao.getStatus() == AvaliacaoStatus.ENVIADA) {
                notas.add(new NotaAvaliadorView(
                    atribuicao.getAvaliador().getId(),
                    atribuicao.getAvaliador().getPessoa().getNome(),
                    avaliacao.getNotaFinal()));
            }
        }

        boolean avaliado = ativas.size() >= edicao.getMinAvaliadoresPorProjeto() && notas.size() == ativas.size();
        BigDecimal notaDoProjeto = notas.isEmpty() ? null : media(notas);
        BigDecimal divergencia = divergencia(notas);
        boolean acimaDoLimite = divergencia != null && edicao.getLimiteDivergenciaNotas() != null
            && divergencia.compareTo(edicao.getLimiteDivergenciaNotas()) > 0;

        return new ResultadoConsolidacao(projeto.getId(), ativas.size(), notas.size(), avaliado,
            notaDoProjeto, divergencia, acimaDoLimite, notas);
    }

    private BigDecimal media(List<NotaAvaliadorView> notas) {
        BigDecimal soma = notas.stream().map(NotaAvaliadorView::notaFinal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(BigDecimal.valueOf(notas.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal divergencia(List<NotaAvaliadorView> notas) {
        if (notas.size() < 2) {
            return null;
        }
        BigDecimal max = notas.stream().map(NotaAvaliadorView::notaFinal).max(BigDecimal::compareTo).orElseThrow();
        BigDecimal min = notas.stream().map(NotaAvaliadorView::notaFinal).min(BigDecimal::compareTo).orElseThrow();
        return max.subtract(min);
    }

    private Projeto buscarProjeto(Long projetoId) {
        return projetoRepository.findById(projetoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Projeto", projetoId));
    }
}
