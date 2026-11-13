package com.avaliacao.sistema.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.AvaliacaoService;
import com.avaliacao.sistema.service.ConsolidacaoService;
import com.avaliacao.sistema.service.dto.ResultadoConsolidacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(controllers = ConsolidacaoController.class)
@Import(WebSecurityTestConfig.class)
class ConsolidacaoControllerTest {

    static final String API = "/api/admin";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ConsolidacaoService consolidacaoService;
    @MockBean AvaliacaoService avaliacaoService;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    @Test
    void consolidarComoAdminRetorna200() throws Exception {
        ResultadoConsolidacao resultado = new ResultadoConsolidacao(7L, 3, 3, true,
            new BigDecimal("7.93"), new BigDecimal("2.30"), false, List.of());
        when(consolidacaoService.consolidar(7L)).thenReturn(resultado);

        mvc.perform(get(API + "/projetos/7/consolidacao").with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notaDoProjeto").value(7.93))
            .andExpect(jsonPath("$.avaliado").value(true));
    }

    @Test
    void consolidarComoAvaliadorRetorna403() throws Exception {
        mvc.perform(get(API + "/projetos/7/consolidacao").with(comoAvaliador)).andExpect(status().isForbidden());
    }

    @Test
    void reabrirComMotivoRetorna204() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("motivo", "Correção de nota"));

        mvc.perform(post(API + "/avaliacoes/40/reabrir").with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isNoContent());

        verify(avaliacaoService).reabrir(40L, "Correção de nota", 1L);
    }

    @Test
    void reabrirSemMotivoRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("motivo", ""));

        mvc.perform(post(API + "/avaliacoes/40/reabrir").with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void reabrirComoAvaliadorRetorna403() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("motivo", "Correção de nota"));

        mvc.perform(post(API + "/avaliacoes/40/reabrir").with(comoAvaliador)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isForbidden());
    }
}
