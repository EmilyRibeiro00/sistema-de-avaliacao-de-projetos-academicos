package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.controller.dto.ProjetoResponse;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.ProjetoService;
import com.avaliacao.sistema.service.dto.CadastroProjetoCommand;
import com.avaliacao.sistema.service.dto.EditarProjetoCommand;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/projetos")
@RequiredArgsConstructor
public class ProjetoCadastroController {

    private final ProjetoService projetoService;

    @GetMapping
    public List<ProjetoResponse> listar(@RequestParam(required = false) Long edicaoId) {
        return projetoService.listar(edicaoId).stream().map(ProjetoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse cadastrar(@Valid @RequestBody CadastroProjetoCommand comando,
                                      @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ProjetoResponse.de(projetoService.cadastrarManualmente(comando, admin.getId()));
    }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EditarProjetoCommand comando,
                                      @AuthenticationPrincipal UsuarioPrincipal admin) {
        return ProjetoResponse.de(projetoService.atualizar(id, comando, admin.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal admin) {
        projetoService.remover(id, admin.getId());
        return ResponseEntity.noContent().build();
    }
}
