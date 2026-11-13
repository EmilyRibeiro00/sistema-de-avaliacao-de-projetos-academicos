package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.CriterioService;
import com.avaliacao.sistema.service.dto.CriterioCommand;
import com.avaliacao.sistema.service.dto.CriterioView;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CriterioController {

    private final CriterioService criterioService;

    @GetMapping("/edicoes/{edicaoId}/criterios")
    public List<CriterioView> listar(@PathVariable Long edicaoId) {
        return criterioService.listar(edicaoId).stream()
            .map(c -> new CriterioView(c.getId(), c.getNome(), c.getPeso(), c.getAtivo()))
            .toList();
    }

    @PostMapping("/edicoes/{edicaoId}/criterios")
    @ResponseStatus(HttpStatus.CREATED)
    public CriterioView adicionar(@PathVariable Long edicaoId, @Valid @RequestBody CriterioCommand comando,
                                  @AuthenticationPrincipal UsuarioPrincipal admin) {
        var criterio = criterioService.adicionar(edicaoId, comando, admin.getId());
        return new CriterioView(criterio.getId(), criterio.getNome(), criterio.getPeso(), criterio.getAtivo());
    }

    @PutMapping("/criterios/{criterioId}")
    public CriterioView atualizar(@PathVariable Long criterioId, @Valid @RequestBody CriterioCommand comando,
                                  @AuthenticationPrincipal UsuarioPrincipal admin) {
        var criterio = criterioService.atualizar(criterioId, comando, admin.getId());
        return new CriterioView(criterio.getId(), criterio.getNome(), criterio.getPeso(), criterio.getAtivo());
    }

    @DeleteMapping("/criterios/{criterioId}")
    public ResponseEntity<Void> remover(@PathVariable Long criterioId, @AuthenticationPrincipal UsuarioPrincipal admin) {
        criterioService.remover(criterioId, admin.getId());
        return ResponseEntity.noContent().build();
    }
}
