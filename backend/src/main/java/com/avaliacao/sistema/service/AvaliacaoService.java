package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Avaliacao;
import com.avaliacao.sistema.entity.AvaliacaoNota;
import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.AvaliacaoStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.exception.AcessoNegadoException;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.AtribuicaoRepository;
import com.avaliacao.sistema.repository.AvaliacaoNotaRepository;
import com.avaliacao.sistema.repository.AvaliacaoRepository;
import com.avaliacao.sistema.repository.CriterioRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.AvaliacaoDetalhe;
import com.avaliacao.sistema.service.dto.CriterioView;
import com.avaliacao.sistema.service.dto.ProjetoAvaliadorView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private static final String PENDENTE = "PENDENTE";

    private final AtribuicaoRepository atribuicaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoNotaRepository avaliacaoNotaRepository;
    private final CriterioRepository criterioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ConsolidacaoService consolidacaoService;
    private final AuditoriaService auditoriaService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<ProjetoAvaliadorView> listarProjetos(Long avaliadorId) {
        return atribuicaoRepository.findByAvaliadorIdAndStatus(avaliadorId, AtribuicaoStatus.ATIVA).stream()
            .map(this::visaoDoProjeto)
            .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoDetalhe detalhar(Long avaliadorId, Long atribuicaoId) {
        Atribuicao atribuicao = atribuicaoDoAvaliador(avaliadorId, atribuicaoId);
        Edicao edicao = atribuicao.getProjeto().getEdicao();
        Avaliacao avaliacao = avaliacaoRepository.findByAtribuicaoId(atribuicaoId).orElse(null);

        Map<Long, BigDecimal> notas = new HashMap<>();
        if (avaliacao != null) {
            avaliacaoNotaRepository.findByAvaliacaoId(avaliacao.getId())
                .forEach(n -> notas.put(n.getCriterio().getId(), n.getNota()));
        }
        List<CriterioView> criterios = criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(edicao.getId()).stream()
            .map(c -> new CriterioView(c.getId(), c.getNome(), c.getPeso(), c.getAtivo()))
            .toList();
        boolean editavel = janelaAberta(edicao) && (avaliacao == null || avaliacao.getStatus() != AvaliacaoStatus.ENVIADA);

        return new AvaliacaoDetalhe(visaoDoProjeto(atribuicao, avaliacao), criterios, notas,
            avaliacao == null ? null : avaliacao.getNotaFinal(), editavel);
    }

    /** Salva notas parciais; pode ser chamado várias vezes até o envio final. */
    @Transactional
    public AvaliacaoDetalhe salvarRascunho(Long avaliadorId, Long atribuicaoId, Map<Long, BigDecimal> notas) {
        gravar(avaliadorId, atribuicaoId, notas, false);
        return detalhar(avaliadorId, atribuicaoId);
    }

    /** Envio definitivo: exige todos os critérios, calcula a nota final e trava a edição pelo avaliador. */
    @Transactional
    public AvaliacaoDetalhe enviar(Long avaliadorId, Long atribuicaoId, Map<Long, BigDecimal> notas) {
        gravar(avaliadorId, atribuicaoId, notas, true);
        return detalhar(avaliadorId, atribuicaoId);
    }

    @Transactional
    public Avaliacao reabrir(Long avaliacaoId, String motivo, Long adminId) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da reabertura é obrigatório");
        }
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliação", avaliacaoId));
        if (avaliacao.getStatus() != AvaliacaoStatus.ENVIADA) {
            throw new RegraNegocioException("Só uma avaliação enviada pode ser reaberta");
        }

        avaliacao.setStatus(AvaliacaoStatus.RASCUNHO);
        avaliacao.setNotaFinal(null);
        avaliacao.setDataEnvio(null);
        avaliacao.setReabertaEm(Instant.now(clock));
        avaliacao.setReabertaPor(usuarioRepository.getReferenceById(adminId));
        avaliacao.setMotivoReabertura(motivo);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);

        consolidacaoService.atualizarStatus(avaliacao.getAtribuicao().getProjeto().getId());
        auditoriaService.registrar(adminId, "AVALIACAO_REABERTA", "Avaliacao", avaliacaoId, motivo);
        return salva;
    }

    // Média ponderada: soma(nota x peso) / soma(pesos), com duas casas decimais
    public static BigDecimal calcularNotaFinal(List<Criterio> criterios, Map<Long, BigDecimal> notas) {
        BigDecimal soma = BigDecimal.ZERO;
        BigDecimal pesos = BigDecimal.ZERO;
        for (Criterio criterio : criterios) {
            soma = soma.add(notas.get(criterio.getId()).multiply(criterio.getPeso()));
            pesos = pesos.add(criterio.getPeso());
        }
        return soma.divide(pesos, 2, RoundingMode.HALF_UP);
    }

    private void gravar(Long avaliadorId, Long atribuicaoId, Map<Long, BigDecimal> notasRecebidas, boolean envio) {
        Atribuicao atribuicao = atribuicaoDoAvaliador(avaliadorId, atribuicaoId);
        Projeto projeto = atribuicao.getProjeto();
        Edicao edicao = projeto.getEdicao();
        if (!janelaAberta(edicao)) {
            throw new RegraNegocioException("Fora do prazo de avaliação desta edição");
        }

        Avaliacao avaliacao = avaliacaoRepository.findByAtribuicaoId(atribuicaoId).orElseGet(() -> {
            Avaliacao nova = new Avaliacao();
            nova.setAtribuicao(atribuicao);
            nova.setStatus(AvaliacaoStatus.RASCUNHO);
            return nova;
        });
        if (avaliacao.getStatus() == AvaliacaoStatus.ENVIADA) {
            throw new RegraNegocioException("Avaliação já enviada: só o administrador pode reabri-la");
        }

        List<Criterio> criterios = criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(edicao.getId());
        validarNotas(notasRecebidas, criterios, edicao);
        avaliacao = avaliacaoRepository.save(avaliacao);

        Map<Long, AvaliacaoNota> existentes = new HashMap<>();
        if (avaliacao.getId() != null) {
            avaliacaoNotaRepository.findByAvaliacaoId(avaliacao.getId())
                .forEach(n -> existentes.put(n.getCriterio().getId(), n));
        }
        for (Criterio criterio : criterios) {
            BigDecimal valor = notasRecebidas.get(criterio.getId());
            if (valor == null) {
                continue;
            }
            AvaliacaoNota nota = existentes.computeIfAbsent(criterio.getId(), id -> {
                AvaliacaoNota n = new AvaliacaoNota();
                n.setCriterio(criterio);
                return n;
            });
            nota.setAvaliacao(avaliacao);
            nota.setNota(valor);
        }
        avaliacaoNotaRepository.saveAll(existentes.values());

        if (envio) {
            Map<Long, BigDecimal> completas = new HashMap<>();
            existentes.forEach((id, n) -> completas.put(id, n.getNota()));
            List<String> faltando = criterios.stream()
                .filter(c -> completas.get(c.getId()) == null).map(Criterio::getNome).toList();
            if (!faltando.isEmpty()) {
                throw new RegraNegocioException("Todos os critérios são obrigatórios. Faltam: " + String.join(", ", faltando));
            }
            avaliacao.setNotaFinal(calcularNotaFinal(criterios, completas));
            avaliacao.setStatus(AvaliacaoStatus.ENVIADA);
            avaliacao.setDataEnvio(Instant.now(clock));
            avaliacaoRepository.save(avaliacao);

            consolidacaoService.atualizarStatus(projeto.getId());
            auditoriaService.registrar(avaliadorId, "AVALIACAO_ENVIADA", "Avaliacao", avaliacao.getId(),
                "nota final " + avaliacao.getNotaFinal());
        }
    }

    private void validarNotas(Map<Long, BigDecimal> notas, List<Criterio> criterios, Edicao edicao) {
        for (Map.Entry<Long, BigDecimal> entrada : notas.entrySet()) {
            if (criterios.stream().noneMatch(c -> c.getId().equals(entrada.getKey()))) {
                throw new RegraNegocioException("Critério " + entrada.getKey() + " não pertence a esta edição");
            }
            BigDecimal valor = entrada.getValue();
            if (valor == null || valor.compareTo(edicao.getEscalaNotaMin()) < 0
                || valor.compareTo(edicao.getEscalaNotaMax()) > 0) {
                throw new RegraNegocioException("A nota deve estar entre " + edicao.getEscalaNotaMin()
                    + " e " + edicao.getEscalaNotaMax());
            }
        }
    }

    // Trava contra acesso a avaliação de outro avaliador (IDOR): a posse é conferida no servidor
    private Atribuicao atribuicaoDoAvaliador(Long avaliadorId, Long atribuicaoId) {
        Atribuicao atribuicao = atribuicaoRepository.findById(atribuicaoId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Atribuição", atribuicaoId));
        if (!atribuicao.getAvaliador().getId().equals(avaliadorId) || atribuicao.getStatus() != AtribuicaoStatus.ATIVA) {
            throw new AcessoNegadoException("Esta atribuição não pertence a você");
        }
        return atribuicao;
    }

    private boolean janelaAberta(Edicao edicao) {
        if (edicao.getStatus() != EdicaoStatus.ATIVA || edicao.getDataAbertura() == null || edicao.getDataFechamento() == null) {
            return false;
        }
        Instant agora = Instant.now(clock);
        return !agora.isBefore(edicao.getDataAbertura()) && !agora.isAfter(edicao.getDataFechamento());
    }

    private ProjetoAvaliadorView visaoDoProjeto(Atribuicao atribuicao) {
        Avaliacao avaliacao = avaliacaoRepository.findByAtribuicaoId(atribuicao.getId()).orElse(null);
        return visaoDoProjeto(atribuicao, avaliacao);
    }

    private ProjetoAvaliadorView visaoDoProjeto(Atribuicao atribuicao, Avaliacao avaliacao) {
        Projeto projeto = atribuicao.getProjeto();
        String status = avaliacao == null ? PENDENTE : avaliacao.getStatus().name();
        return new ProjetoAvaliadorView(atribuicao.getId(), projeto.getId(), projeto.getTitulo(),
            projeto.getComite() == null ? null : projeto.getComite().getNome(), projeto.getConteudoTecnico(), status);
    }
}
