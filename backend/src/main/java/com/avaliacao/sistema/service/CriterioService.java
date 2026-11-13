package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.CriterioRepository;
import com.avaliacao.sistema.service.dto.CriterioCommand;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CriterioService {

    private final CriterioRepository criterioRepository;
    private final EdicaoService edicaoService;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Criterio> listar(Long edicaoId) {
        return criterioRepository.findByEdicaoIdAndAtivoTrueOrderByOrdem(edicaoId);
    }

    @Transactional
    public Criterio adicionar(Long edicaoId, CriterioCommand cmd, Long adminId) {
        Edicao edicao = edicaoService.buscar(edicaoId);
        edicaoService.garantirEditavel(edicao);
        validarPeso(cmd.peso());

        Criterio criterio = new Criterio();
        criterio.setEdicao(edicao);
        criterio.setNome(cmd.nome());
        criterio.setPeso(cmd.peso());
        criterio.setOrdem(cmd.ordem() == null ? listar(edicaoId).size() + 1 : cmd.ordem());
        Criterio salvo = criterioRepository.save(criterio);

        auditoriaService.registrar(adminId, "CRITERIO_CRIADO", "Criterio", salvo.getId(), salvo.getNome());
        return salvo;
    }

    @Transactional
    public Criterio atualizar(Long criterioId, CriterioCommand cmd, Long adminId) {
        Criterio criterio = buscar(criterioId);
        edicaoService.garantirEditavel(criterio.getEdicao());
        validarPeso(cmd.peso());

        criterio.setNome(cmd.nome());
        criterio.setPeso(cmd.peso());
        if (cmd.ordem() != null) {
            criterio.setOrdem(cmd.ordem());
        }
        Criterio salvo = criterioRepository.save(criterio);

        auditoriaService.registrar(adminId, "CRITERIO_ATUALIZADO", "Criterio", salvo.getId(), salvo.getNome());
        return salvo;
    }

    /** Remoção lógica: marca ativo=false em vez de apagar, para preservar as notas já lançadas com este critério. */
    @Transactional
    public void remover(Long criterioId, Long adminId) {
        Criterio criterio = buscar(criterioId);
        edicaoService.garantirEditavel(criterio.getEdicao());
        criterio.setAtivo(false);
        criterioRepository.save(criterio);
        auditoriaService.registrar(adminId, "CRITERIO_REMOVIDO", "Criterio", criterioId, criterio.getNome());
    }

    @Transactional(readOnly = true)
    public BigDecimal somaPesos(Long edicaoId) {
        return listar(edicaoId).stream().map(Criterio::getPeso).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Criterio buscar(Long criterioId) {
        return criterioRepository.findById(criterioId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Critério", criterioId));
    }

    private void validarPeso(BigDecimal peso) {
        if (peso.signum() <= 0) {
            throw new RegraNegocioException("O peso do critério deve ser maior que zero");
        }
    }
}
