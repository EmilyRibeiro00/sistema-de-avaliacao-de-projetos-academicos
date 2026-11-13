package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.EdicaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(controllers = EdicaoController.class)
@Import(WebSecurityTestConfig.class)
class EdicaoControllerTest {

    static final String API = "/api/admin/edicoes";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean EdicaoService service;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    private Edicao edicaoFake() {
        Edicao e = new Edicao();
        e.setId(10L);
        e.setNome("Mostra 2026");
        e.setAno(2026);
        e.setStatus(EdicaoStatus.PLANEJADA);
        e.setMinAvaliadoresPorProjeto(3);
        e.setEscalaNotaMin(BigDecimal.ZERO);
        e.setEscalaNotaMax(BigDecimal.TEN);
        return e;
    }

    @Test
    void listarComoAdminRetorna200ComAsEdicoes() throws Exception {
        when(service.listar()).thenReturn(List.of(edicaoFake()));

        mvc.perform(get(API).with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nome").value("Mostra 2026"))
            .andExpect(jsonPath("$[0].minAvaliadoresPorProjeto").value(3));
    }

    @Test
    void listarSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(get(API)).andExpect(status().isUnauthorized());
    }

    @Test
    void listarComoAvaliadorRetorna403() throws Exception {
        mvc.perform(get(API).with(comoAvaliador)).andExpect(status().isForbidden());
    }

    @Test
    void criarComoAdminRetorna201() throws Exception {
        when(service.criar(any(), eq(1L))).thenReturn(edicaoFake());

        String json = objectMapper.writeValueAsString(Map.of(
            "nome", "Mostra 2026",
            "ano", 2026,
            "dataAbertura", Instant.parse("2026-09-01T00:00:00Z"),
            "dataFechamento", Instant.parse("2026-12-01T00:00:00Z")));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.nome").value("Mostra 2026"));
    }

    @Test
    void criarSemNomeRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "nome", "",
            "ano", 2026,
            "dataAbertura", Instant.parse("2026-09-01T00:00:00Z"),
            "dataFechamento", Instant.parse("2026-12-01T00:00:00Z")));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void criarComoAvaliadorRetorna403() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "nome", "Mostra 2026",
            "ano", 2026,
            "dataAbertura", Instant.parse("2026-09-01T00:00:00Z"),
            "dataFechamento", Instant.parse("2026-12-01T00:00:00Z")));

        mvc.perform(post(API).with(comoAvaliador).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isForbidden());
    }

    @Test
    void ativarComoAdminRetorna200() throws Exception {
        Edicao ativa = edicaoFake();
        ativa.setStatus(EdicaoStatus.ATIVA);
        when(service.ativar(10L, 1L)).thenReturn(ativa);

        mvc.perform(post(API + "/10/ativar").with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ATIVA"));
    }
}
