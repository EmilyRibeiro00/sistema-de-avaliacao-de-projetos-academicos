package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.CriterioService;
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

@WebMvcTest(controllers = CriterioController.class)
@Import(WebSecurityTestConfig.class)
class CriterioControllerTest {

    static final String API = "/api/admin";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CriterioService service;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    private Criterio criterioFake() {
        Criterio c = new Criterio();
        c.setId(5L);
        c.setNome("Clareza");
        c.setPeso(new BigDecimal("30"));
        return c;
    }

    @Test
    void listarComoAdminRetorna200() throws Exception {
        when(service.listar(10L)).thenReturn(List.of(criterioFake()));

        mvc.perform(get(API + "/edicoes/10/criterios").with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Clareza"));
    }

    @Test
    void listarComoAvaliadorRetorna403() throws Exception {
        mvc.perform(get(API + "/edicoes/10/criterios").with(comoAvaliador)).andExpect(status().isForbidden());
    }

    @Test
    void adicionarComoAdminRetorna201() throws Exception {
        when(service.adicionar(eq(10L), any(), eq(1L))).thenReturn(criterioFake());

        String json = objectMapper.writeValueAsString(Map.of("nome", "Clareza", "peso", 30));

        mvc.perform(post(API + "/edicoes/10/criterios").with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.nome").value("Clareza"));
    }

    @Test
    void adicionarSemNomeRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("nome", "", "peso", 30));

        mvc.perform(post(API + "/edicoes/10/criterios").with(comoAdmin)
                .contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void removerComoAdminRetorna204() throws Exception {
        mvc.perform(delete(API + "/criterios/5").with(comoAdmin)).andExpect(status().isNoContent());

        verify(service).remover(5L, 1L);
    }

    @Test
    void removerComoAvaliadorRetorna403() throws Exception {
        mvc.perform(delete(API + "/criterios/5").with(comoAvaliador)).andExpect(status().isForbidden());
    }
}
