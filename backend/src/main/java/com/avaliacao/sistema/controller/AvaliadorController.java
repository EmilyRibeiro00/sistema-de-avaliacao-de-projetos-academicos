package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.AvaliacaoService;
import com.avaliacao.sistema.service.dto.AvaliacaoDetalhe;
import com.avaliacao.sistema.service.dto.ProjetoAvaliadorView;
import com.avaliacao.sistema.controller.dto.NotasRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// O avaliador só enxerga o que foi atribuído a ele, e sempre em visão cega
@RestController
@RequestMapping("/api/avaliador")
@RequiredArgsConstructor
public class AvaliadorController {

    private final AvaliacaoService avaliacaoService;

    @GetMapping("/projetos")
    public List<ProjetoAvaliadorView> projetos(@AuthenticationPrincipal UsuarioPrincipal avaliador) {
        return avaliacaoService.listarProjetos(avaliador.getId());
    }

    @GetMapping("/atribuicoes/{atribuicaoId}")
    public AvaliacaoDetalhe detalhar(@PathVariable Long atribuicaoId, @AuthenticationPrincipal UsuarioPrincipal avaliador) {
        return avaliacaoService.detalhar(avaliador.getId(), atribuicaoId);
    }

    @PutMapping("/atribuicoes/{atribuicaoId}/rascunho")
    public AvaliacaoDetalhe salvarRascunho(@PathVariable Long atribuicaoId, @Valid @RequestBody NotasRequest request,
                                           @AuthenticationPrincipal UsuarioPrincipal avaliador) {
        return avaliacaoService.salvarRascunho(avaliador.getId(), atribuicaoId, request.notas());
    }

    @PostMapping("/atribuicoes/{atribuicaoId}/enviar")
    public AvaliacaoDetalhe enviar(@PathVariable Long atribuicaoId, @Valid @RequestBody NotasRequest request,
                                   @AuthenticationPrincipal UsuarioPrincipal avaliador) {
        return avaliacaoService.enviar(avaliador.getId(), atribuicaoId, request.notas());
    }
}
