package com.avaliacao.sistema.controller;

import com.avaliacao.sistema.security.CustomUserDetailsService;
import com.avaliacao.sistema.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

// Réplica mínima das regras de autorização de SecurityConfig, só pros testes de @WebMvcTest.
// A autenticação nos testes é injetada direto via SecurityMockMvcRequestPostProcessors.authentication(...),
// então o JwtAuthenticationFilter nunca faz nada de fato — mas o @WebMvcTest detecta e instancia
// automaticamente qualquer Filter do classpath, então precisa mockar as dependências dele aqui pra satisfazer o construtor.
@TestConfiguration
public class WebSecurityTestConfig {

    @MockBean JwtService jwtService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/avaliador/**").hasRole("AVALIADOR")
                .anyRequest().authenticated())
            // Mesma distinção 401 (sem autenticação) x 403 (autenticado, papel errado) do SecurityConfig
            // de produção — sem isso os testes de "sem autenticação" não refletiriam o comportamento real.
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erro\":\"Autenticação necessária\"}");
        };
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"erro\":\"Acesso negado\"}");
        };
    }
}
