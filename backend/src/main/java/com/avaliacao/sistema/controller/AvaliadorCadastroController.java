package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.controller.dto.AvaliadorListResponse;
import com.avaliacao.sistema.controller.dto.AvaliadorResponse;
import com.avaliacao.sistema.controller.dto.ParticipacaoResponse;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.security.UsuarioPrincipal;
import com.avaliacao.sistema.service.EdicaoAvaliadorService;
import com.avaliacao.sistema.service.UsuarioService;
import com.avaliacao.sistema.service.dto.CadastroAvaliadorCommand;
import com.avaliacao.sistema.service.dto.EditarAvaliadorCommand;
import com.avaliacao.sistema.service.dto.VincularAvaliadorCommand;
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
@RequestMapping("/api/admin/avaliadores")
@RequiredArgsConstructor
public class AvaliadorCadastroController {

    private final UsuarioService usuarioService;
    private final EdicaoAvaliadorService edicaoAvaliadorService;

    @GetMapping
    public List<AvaliadorListResponse> listar() {
        return usuarioService.listar().stream().map(AvaliadorListResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliadorResponse cadastrar(@Valid @RequestBody CadastroAvaliadorCommand comando,
                                        @AuthenticationPrincipal UsuarioPrincipal admin) {
        Usuario criado = usuarioService.cadastrarAvaliador(comando, admin.getId());
        return AvaliadorResponse.de(criado, usuarioService.getSenhaPadraoAvaliador());
    }

    @PutMapping("/{id}")
    public AvaliadorListResponse atualizar(@PathVariable Long id, @Valid @RequestBody EditarAvaliadorCommand comando,
                                            @AuthenticationPrincipal UsuarioPrincipal admin) {
        return AvaliadorListResponse.de(usuarioService.atualizar(id, comando, admin.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal admin) {
        usuarioService.remover(id, admin.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{avaliadorId}/edicoes")
    public ParticipacaoResponse vincular(@PathVariable Long avaliadorId, @Valid @RequestBody VincularAvaliadorCommand comando,
                                          @AuthenticationPrincipal UsuarioPrincipal admin) {
        var participacao = edicaoAvaliadorService.vincular(avaliadorId, comando, admin.getId());
        return ParticipacaoResponse.de(participacao);
    }
}
