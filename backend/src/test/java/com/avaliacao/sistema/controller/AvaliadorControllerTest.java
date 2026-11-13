package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.AvaliacaoService;
import com.avaliacao.sistema.service.dto.AvaliacaoDetalhe;
import com.avaliacao.sistema.service.dto.CriterioView;
import com.avaliacao.sistema.service.dto.ProjetoAvaliadorView;
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

@WebMvcTest(controllers = AvaliadorController.class)
@Import(WebSecurityTestConfig.class)
class AvaliadorControllerTest {

    static final String API = "/api/avaliador";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AvaliacaoService service;

    RequestPostProcessor comoAvaliador;
    RequestPostProcessor comoAdmin;

    @BeforeEach
    void setUp() {
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
    }

    private ProjetoAvaliadorView projetoView() {
        return new ProjetoAvaliadorView(50L, 7L, "Projeto de teste", "Computação", "Conteúdo técnico", "PENDENTE");
    }

    @Test
    void projetosComoAvaliadorRetorna200EmVisaoCega() throws Exception {
        when(service.listarProjetos(2L)).thenReturn(List.of(projetoView()));

        mvc.perform(get(API + "/projetos").with(comoAvaliador))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titulo").value("Projeto de teste"))
            .andExpect(jsonPath("$[0].comite").exists());
    }

    @Test
    void projetosComoAdminRetorna403() throws Exception {
        mvc.perform(get(API + "/projetos").with(comoAdmin)).andExpect(status().isForbidden());
    }

    @Test
    void projetosSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(get(API + "/projetos")).andExpect(status().isUnauthorized());
    }

    @Test
    void detalharRetornaFormularioDaAvaliacao() throws Exception {
        AvaliacaoDetalhe detalhe = new AvaliacaoDetalhe(projetoView(),
            List.of(new CriterioView(1L, "Clareza", new BigDecimal("30"), true)), Map.of(), null, true);
        when(service.detalhar(2L, 50L)).thenReturn(detalhe);

        mvc.perform(get(API + "/atribuicoes/50").with(comoAvaliador))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.editavel").value(true))
            .andExpect(jsonPath("$.criterios[0].nome").value("Clareza"));
    }

    @Test
    void salvarRascunhoComNotasRetorna200() throws Exception {
        AvaliacaoDetalhe detalhe = new AvaliacaoDetalhe(projetoView(), List.of(), Map.of(1L, new BigDecimal("8")), null, true);
        when(service.salvarRascunho(eq(2L), eq(50L), any())).thenReturn(detalhe);

        String json = objectMapper.writeValueAsString(Map.of("notas", Map.of("1", 8)));

        mvc.perform(put(API + "/atribuicoes/50/rascunho").with(comoAvaliador)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk());
    }

    @Test
    void enviarSemNotasRetorna400() throws Exception {
        String json = "{}";

        mvc.perform(post(API + "/atribuicoes/50/enviar").with(comoAvaliador)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void enviarComNotasRetorna200ComNotaFinal() throws Exception {
        AvaliacaoDetalhe detalhe = new AvaliacaoDetalhe(projetoView(), List.of(),
            Map.of(1L, new BigDecimal("8")), new BigDecimal("8.10"), false);
        when(service.enviar(eq(2L), eq(50L), any())).thenReturn(detalhe);

        String json = objectMapper.writeValueAsString(Map.of("notas", Map.of("1", 8, "2", 9, "3", 7)));

        mvc.perform(post(API + "/atribuicoes/50/enviar").with(comoAvaliador)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notaFinal").value(8.10))
            .andExpect(jsonPath("$.editavel").value(false));
    }
}
