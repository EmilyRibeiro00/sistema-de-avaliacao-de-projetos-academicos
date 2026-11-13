package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.ProjetoService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(controllers = ProjetoCadastroController.class)
@Import(WebSecurityTestConfig.class)
class ProjetoCadastroControllerTest {

    static final String API = "/api/admin/projetos";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProjetoService service;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    private Projeto projetoFake() {
        Edicao edicao = new Edicao();
        edicao.setId(10L);

        Projeto projeto = new Projeto();
        projeto.setId(5L);
        projeto.setEdicao(edicao);
        projeto.setSigaaId("MANUAL-abc");
        projeto.setTitulo("Projeto de teste");
        projeto.setStatus(ProjetoStatus.SUBMETIDO);
        return projeto;
    }

    private String jsonValido() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "edicaoId", 10,
            "titulo", "Projeto de teste",
            "conteudoTecnico", "Conteúdo",
            "autorNome", "Fulano"));
    }

    @Test
    void cadastrarComoAdminRetorna201() throws Exception {
        when(service.cadastrarManualmente(any(), eq(1L))).thenReturn(projetoFake());

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.status").value("SUBMETIDO"));
    }

    @Test
    void cadastrarComoAvaliadorRetorna403() throws Exception {
        mvc.perform(post(API).with(comoAvaliador).contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
            .andExpect(status().isForbidden());
    }

    @Test
    void cadastrarSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(post(API).contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastrarSemTituloRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "edicaoId", 10,
            "titulo", "",
            "conteudoTecnico", "Conteúdo",
            "autorNome", "Fulano"));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void cadastrarSemAutorRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "edicaoId", 10,
            "titulo", "Projeto de teste",
            "conteudoTecnico", "Conteúdo"));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarSemEdicaoIdRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "titulo", "Projeto de teste",
            "conteudoTecnico", "Conteúdo",
            "autorNome", "Fulano"));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }
}
