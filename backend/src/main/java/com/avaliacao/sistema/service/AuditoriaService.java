package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.LogAuditoria;
import com.avaliacao.sistema.repository.LogAuditoriaRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    // usuarioId nulo = ação feita pelo próprio sistema (ex.: atribuição automática)
    @Transactional
    public void registrar(Long usuarioId, String acao, String entidade, Long entidadeId, String detalhes) {
        LogAuditoria log = new LogAuditoria();
        log.setUsuario(usuarioId == null ? null : usuarioRepository.getReferenceById(usuarioId));
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setDetalhes(detalhes);
        log.setDataHora(Instant.now(clock));
        logAuditoriaRepository.save(log);
    }
}
