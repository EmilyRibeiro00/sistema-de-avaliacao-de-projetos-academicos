package com.avaliacao.sistema.controller.dto;

import com.avaliacao.sistema.entity.Usuario;

// senhaTemporaria só aparece aqui porque ainda não existe envio de e-mail de verdade (ver EmailService) —
// é o jeito do admin conseguir repassar a senha padrão manualmente até isso existir. Tirar este
// campo da resposta quando um provedor de e-mail de verdade estiver configurado.
public record AvaliadorResponse(Long id, String nome, String email, String senhaTemporaria) {

    public static AvaliadorResponse de(Usuario usuario, String senhaTemporaria) {
        return new AvaliadorResponse(usuario.getId(), usuario.getPessoa().getNome(), usuario.getPessoa().getEmail(),
            senhaTemporaria);
    }
}
