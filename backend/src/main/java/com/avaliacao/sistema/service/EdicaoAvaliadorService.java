package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.EdicaoAvaliadorStatus;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.exception.EntidadeNaoEncontradaException;
import com.avaliacao.sistema.exception.RegraNegocioException;
import com.avaliacao.sistema.repository.EdicaoAvaliadorRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import com.avaliacao.sistema.service.dto.VincularAvaliadorCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EdicaoAvaliadorService {

    private final EdicaoAvaliadorRepository edicaoAvaliadorRepository;
    private final CatalogoService catalogoService;
    private final UsuarioRepository usuarioRepository;
    private final EdicaoService edicaoService;
    private final AuditoriaService auditoriaService;

    /**
     * Um avaliador só fica ATIVO em uma edição por vez: vincular a uma edição nova libera (INATIVO)
     * qualquer outra em que ele estivesse ativo, pra o sorteio só considerar avaliadores livres.
     *
     * Comitê é fixo pro avaliador (vale em todas as edições); campus é por edição (o mesmo avaliador
     * pode atuar num campus numa edição e noutro na seguinte). Os dois são obrigatórios pra ativar,
     * porque o banco exige (chk_usuario_avaliador_comite, chk_edicao_avaliador_ativo_campus).
     */
    @Transactional
    public EdicaoAvaliador vincular(Long avaliadorId, VincularAvaliadorCommand cmd, Long adminId) {
        Usuario avaliador = usuarioRepository.findById(avaliadorId)
            .orElseThrow(() -> new EntidadeNaoEncontradaException("Avaliador", avaliadorId));
        if (avaliador.getTipo() != UsuarioTipo.AVALIADOR) {
            throw new RegraNegocioException("Usuário informado não é um avaliador");
        }
        var edicao = edicaoService.buscar(cmd.edicaoId());
        edicaoService.garantirEditavel(edicao);

        Comite comite = catalogoService.resolverComite(cmd.comiteNome());
        if (comite == null) {
            throw new RegraNegocioException("O comitê do avaliador é obrigatório");
        }
        Campus campus = catalogoService.resolverCampus(cmd.campusNome());
        if (campus == null) {
            throw new RegraNegocioException("O campus de atuação do avaliador nesta edição é obrigatório");
        }

        avaliador.setComite(comite);
        usuarioRepository.save(avaliador);

        edicaoAvaliadorRepository.findByAvaliadorIdAndStatus(avaliadorId, EdicaoAvaliadorStatus.ATIVO).stream()
            .filter(outra -> !outra.getEdicao().getId().equals(edicao.getId()))
            .forEach(outra -> {
                outra.setStatus(EdicaoAvaliadorStatus.INATIVO);
                edicaoAvaliadorRepository.save(outra);
            });

        EdicaoAvaliador participacao = edicaoAvaliadorRepository.findByEdicaoIdAndAvaliadorId(edicao.getId(), avaliadorId)
            .orElseGet(() -> {
                EdicaoAvaliador nova = new EdicaoAvaliador();
                nova.setEdicao(edicao);
                nova.setAvaliador(avaliador);
                return nova;
            });
        participacao.setStatus(EdicaoAvaliadorStatus.ATIVO);
        participacao.setLimiteMaxProjetos(cmd.limiteMaxProjetos());
        participacao.setCampus(campus);
        EdicaoAvaliador salva = edicaoAvaliadorRepository.save(participacao);

        auditoriaService.registrar(adminId, "AVALIADOR_VINCULADO_EDICAO", "EdicaoAvaliador", salva.getId(),
            "avaliador=" + avaliadorId + ", edicao=" + edicao.getId());
        return salva;
    }
}
