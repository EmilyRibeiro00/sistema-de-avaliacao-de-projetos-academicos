package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.EdicaoService;
import com.avaliacao.sistema.service.dto.EdicaoCommand;
import com.avaliacao.sistema.controller.dto.EdicaoResponse;
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
@RequestMapping("/api/admin/edicoes")
@RequiredArgsConstructor
public class EdicaoController {

    private final EdicaoService edicaoService;

    @GetMapping
    public List<EdicaoResponse> listar() {
        return edicaoService.listar().stream().map(EdicaoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public EdicaoResponse buscar(@PathVariable Long id) {
        return EdicaoResponse.de(edicaoService.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EdicaoResponse criar(@Valid @RequestBody EdicaoCommand comando, @AuthenticationPrincipal UsuarioPrincipal admin) {
        return EdicaoResponse.de(edicaoService.criar(comando, admin.getId()));
    }

    @PutMapping("/{id}")
    public EdicaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EdicaoCommand comando,
                                     @AuthenticationPrincipal UsuarioPrincipal admin) {
        return EdicaoResponse.de(edicaoService.atualizar(id, comando, admin.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal admin) {
        edicaoService.remover(id, admin.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ativar")
    public EdicaoResponse ativar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal admin) {
        return EdicaoResponse.de(edicaoService.ativar(id, admin.getId()));
    }

    @PostMapping("/{id}/encerrar")
    public EdicaoResponse encerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal admin) {
        return EdicaoResponse.de(edicaoService.encerrar(id, admin.getId()));
    }
}
