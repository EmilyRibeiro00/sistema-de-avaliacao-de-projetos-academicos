package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.AvaliacaoService;
import com.avaliacao.sistema.service.ConsolidacaoService;
import com.avaliacao.sistema.service.dto.ResultadoConsolidacao;
import com.avaliacao.sistema.controller.dto.MotivoRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ConsolidacaoController {

    private final ConsolidacaoService consolidacaoService;
    private final AvaliacaoService avaliacaoService;

    // Só o admin vê a comparação entre avaliadores
    @GetMapping("/projetos/{projetoId}/consolidacao")
    public ResultadoConsolidacao consolidar(@PathVariable Long projetoId) {
        return consolidacaoService.consolidar(projetoId);
    }

    @PostMapping("/avaliacoes/{avaliacaoId}/reabrir")
    public ResponseEntity<Void> reabrir(@PathVariable Long avaliacaoId, @Valid @RequestBody MotivoRequest request,
                                        @AuthenticationPrincipal UsuarioPrincipal admin) {
        avaliacaoService.reabrir(avaliacaoId, request.motivo(), admin.getId());
        return ResponseEntity.noContent().build();
    }
}
