package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.EdicaoRepository;
import com.avaliacao.sistema.service.dto.EdicaoCommand;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EdicaoService {

    private static final int MIN_AVALIADORES_PADRAO = 3;

    private final EdicaoRepository edicaoRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Edicao> listar() {
        return edicaoRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Edicao buscar(Long id) {
        return edicaoRepository.findById(id).orElseThrow(() -> new EntidadeNaoEncontradaException("Edição", id));
    }

    /** Usado por quem escreve dados presos a uma edição (critério, projeto, vínculo de avaliador). */
    public void garantirEditavel(Edicao edicao) {
        if (edicao.getStatus() == EdicaoStatus.ENCERRADA) {
            throw new RegraNegocioException("Edição encerrada é somente leitura");
        }
    }

    @Transactional(readOnly = true)
    public Edicao buscarAtiva() {
        return edicaoRepository.findByStatus(EdicaoStatus.ATIVA)
            .orElseThrow(() -> new RegraNegocioException("Não há edição ativa"));
    }

    @Transactional
    public Edicao criar(EdicaoCommand cmd, Long adminId) {
        ValoresValidados v = validar(cmd);

        Edicao edicao = new Edicao();
        edicao.setNome(cmd.nome());
        edicao.setAno(cmd.ano());
        edicao.setStatus(EdicaoStatus.PLANEJADA);
        edicao.setDataAbertura(cmd.dataAbertura());
        edicao.setDataFechamento(cmd.dataFechamento());
        edicao.setMinAvaliadoresPorProjeto(v.minimo());
        edicao.setLimiteMaxProjetosPorAvaliador(cmd.limiteMaxProjetosPorAvaliador());
        edicao.setLimiteDivergenciaNotas(cmd.limiteDivergenciaNotas());
        edicao.setEscalaNotaMin(v.escalaMin());
        edicao.setEscalaNotaMax(v.escalaMax());
        Edicao salva = edicaoRepository.save(edicao);

        auditoriaService.registrar(adminId, "EDICAO_CRIADA", "Edicao", salva.getId(), salva.getNome());
        return salva;
    }

    @Transactional
    public Edicao atualizar(Long id, EdicaoCommand cmd, Long adminId) {
        Edicao edicao = buscar(id);
        garantirEditavel(edicao);
        ValoresValidados v = validar(cmd);

        edicao.setNome(cmd.nome());
        edicao.setAno(cmd.ano());
        edicao.setDataAbertura(cmd.dataAbertura());
        edicao.setDataFechamento(cmd.dataFechamento());
        edicao.setMinAvaliadoresPorProjeto(v.minimo());
        edicao.setLimiteMaxProjetosPorAvaliador(cmd.limiteMaxProjetosPorAvaliador());
        edicao.setLimiteDivergenciaNotas(cmd.limiteDivergenciaNotas());
        edicao.setEscalaNotaMin(v.escalaMin());
        edicao.setEscalaNotaMax(v.escalaMax());
        Edicao salva = edicaoRepository.save(edicao);

        auditoriaService.registrar(adminId, "EDICAO_ATUALIZADA", "Edicao", id, salva.getNome());
        return salva;
    }

    /** Remoção lógica: marca ativo=false em vez de apagar. Uma edição ativa precisa ser encerrada antes. */
    @Transactional
    public void remover(Long id, Long adminId) {
        Edicao edicao = buscar(id);
        if (edicao.getStatus() == EdicaoStatus.ATIVA) {
            throw new RegraNegocioException("Encerre a edição antes de removê-la");
        }
        edicao.setAtivo(false);
        edicaoRepository.save(edicao);
        auditoriaService.registrar(adminId, "EDICAO_REMOVIDA", "Edicao", id, edicao.getNome());
    }

    private ValoresValidados validar(EdicaoCommand cmd) {
        if (!cmd.dataFechamento().isAfter(cmd.dataAbertura())) {
            throw new RegraNegocioException("A data de fechamento deve ser posterior à de abertura");
        }
        int minimo = cmd.minAvaliadoresPorProjeto() == null ? MIN_AVALIADORES_PADRAO : cmd.minAvaliadoresPorProjeto();
        if (minimo < 1) {
            throw new RegraNegocioException("O mínimo de avaliadores por projeto deve ser pelo menos 1");
        }
        BigDecimal escalaMin = cmd.escalaNotaMin() == null ? BigDecimal.ZERO : cmd.escalaNotaMin();
        BigDecimal escalaMax = cmd.escalaNotaMax() == null ? BigDecimal.TEN : cmd.escalaNotaMax();
        if (escalaMin.compareTo(escalaMax) >= 0) {
            throw new RegraNegocioException("A escala de notas é inválida: o mínimo deve ser menor que o máximo");
        }
        return new ValoresValidados(minimo, escalaMin, escalaMax);
    }

    private record ValoresValidados(int minimo, BigDecimal escalaMin, BigDecimal escalaMax) {
    }

    // Só uma edição pode estar ATIVA por vez
    @Transactional
    public Edicao ativar(Long id, Long adminId) {
        Edicao edicao = buscar(id);
        if (edicao.getStatus() == EdicaoStatus.ENCERRADA) {
            throw new RegraNegocioException("Uma edição encerrada não pode ser reativada");
        }
        edicaoRepository.findByStatus(EdicaoStatus.ATIVA)
            .filter(outra -> !outra.getId().equals(id))
            .ifPresent(outra -> {
                throw new RegraNegocioException("Já existe uma edição ativa: " + outra.getNome());
            });

        edicao.setStatus(EdicaoStatus.ATIVA);
        Edicao salva = edicaoRepository.save(edicao);
        auditoriaService.registrar(adminId, "EDICAO_ATIVADA", "Edicao", id, salva.getNome());
        return salva;
    }

    @Transactional
    public Edicao encerrar(Long id, Long adminId) {
        Edicao edicao = buscar(id);
        if (edicao.getStatus() != EdicaoStatus.ATIVA) {
            throw new RegraNegocioException("Só uma edição ativa pode ser encerrada");
        }
        edicao.setStatus(EdicaoStatus.ENCERRADA);
        Edicao salva = edicaoRepository.save(edicao);
        auditoriaService.registrar(adminId, "EDICAO_ENCERRADA", "Edicao", id, salva.getNome());
        return salva;
    }
}
