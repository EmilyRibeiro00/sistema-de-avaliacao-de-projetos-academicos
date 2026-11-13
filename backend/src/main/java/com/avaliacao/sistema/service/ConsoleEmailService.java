package com.avaliacao.sistema.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// Implementação provisória: não envia nada, só loga. Nenhum provedor de e-mail está configurado
// ainda (sem SMTP/API key) — ver EmailService para o que precisa ser trocado quando houver um.
@Slf4j
@Service
public class ConsoleEmailService implements EmailService {

    @Override
    public void enviarCredenciaisIniciais(String destinatario, String nome, String senhaPadrao) {
        log.info("[e-mail simulado] Para: {} — Olá {}, sua senha de acesso é: {} (troque no primeiro login)",
            destinatario, nome, senhaPadrao);
    }
}
