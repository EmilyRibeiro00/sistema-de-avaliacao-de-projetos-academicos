package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.AtribuicaoService;
import com.avaliacao.sistema.service.dto.ResultadoAtribuicao;
import com.avaliacao.sistema.controller.dto.AtribuicaoResponse;
import com.avaliacao.sistema.controller.dto.ReatribuicaoRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/atribuicoes")
@RequiredArgsConstructor
public class AtribuicaoController {

    private final AtribuicaoService atribuicaoService;

    @PostMapping("/projetos/{projetoId}")
    public ResultadoAtribuicao atribuirProjeto(@PathVariable Long projetoId) {
        return atribuicaoService.atribuirProjeto(projetoId);
    }

    @PostMapping("/edicao-ativa")
    public List<ResultadoAtribuicao> atribuirEdicaoAtiva() {
        return atribuicaoService.atribuirEdicaoAtiva();
    }

    @PostMapping("/{atribuicaoId}/reatribuir")
    public AtribuicaoResponse reatribuir(@PathVariable Long atribuicaoId, @Valid @RequestBody ReatribuicaoRequest request,
                                         @AuthenticationPrincipal UsuarioPrincipal admin) {
        return AtribuicaoResponse.de(
            atribuicaoService.reatribuir(atribuicaoId, request.novoAvaliadorId(), request.motivo(), admin.getId()));
    }
}
