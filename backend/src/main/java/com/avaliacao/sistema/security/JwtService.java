package com.avaliacao.sistema.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMinutos;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiracao-minutos}") long expiracaoMinutos
    ) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(UsuarioPrincipal principal) {
        Instant agora = Instant.now();
        return Jwts.builder()
            .subject(principal.getUsername())
            .claim("usuarioId", principal.getId())
            .claim("tipo", principal.getUsuario().getTipo().name())
            .claim("precisaTrocarSenha", principal.getUsuario().getPrecisaTrocarSenha())
            .issuedAt(Date.from(agora))
            .expiration(Date.from(agora.plus(expiracaoMinutos, ChronoUnit.MINUTES)))
            .signWith(chave)
            .compact();
    }

    public String extrairSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(chave)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
