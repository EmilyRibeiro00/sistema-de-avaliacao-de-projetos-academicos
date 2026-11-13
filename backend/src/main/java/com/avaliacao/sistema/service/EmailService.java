package com.avaliacao.sistema.service;

// Ponto de troca único: implemente esta interface com JavaMailSender (ou um provedor como
// SendGrid/SES) e registre o bean no lugar de ConsoleEmailService quando o envio de verdade
// for configurado.
public interface EmailService {

    void enviarCredenciaisIniciais(String destinatario, String nome, String senhaPadrao);
}
