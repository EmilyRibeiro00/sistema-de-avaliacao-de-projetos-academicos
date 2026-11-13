package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.AtribuicaoOrigem;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.AtribuicaoService;
import com.avaliacao.sistema.service.dto.ResultadoAtribuicao;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(controllers = AtribuicaoController.class)
@Import(WebSecurityTestConfig.class)
class AtribuicaoControllerTest {

    static final String API = "/api/admin/atribuicoes";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AtribuicaoService service;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    @Test
    void atribuirProjetoComoAdminRetorna200() throws Exception {
        ResultadoAtribuicao resultado = new ResultadoAtribuicao(7L, 0, 3, 0, List.of(11L, 12L, 13L), List.of());
        when(service.atribuirProjeto(7L)).thenReturn(resultado);

        mvc.perform(post(API + "/projetos/7").with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.novosAtribuidos").value(3))
            .andExpect(jsonPath("$.faltantes").value(0));
    }

    @Test
    void atribuirProjetoComoAvaliadorRetorna403() throws Exception {
        mvc.perform(post(API + "/projetos/7").with(comoAvaliador)).andExpect(status().isForbidden());
    }

    @Test
    void atribuirEdicaoAtivaRetornaListaDeResultados() throws Exception {
        when(service.atribuirEdicaoAtiva()).thenReturn(List.of(
            new ResultadoAtribuicao(1L, 0, 3, 0, List.of(11L, 12L, 13L), List.of()),
            new ResultadoAtribuicao(2L, 0, 2, 1, List.of(11L, 12L), List.of("Pool insuficiente"))));

        mvc.perform(post(API + "/edicao-ativa").with(comoAdmin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
            .andExpect(jsonPath("$[1].faltantes").value(1));
    }

    @Test
    void reatribuirComMotivoRetornaAtribuicaoAtualizada() throws Exception {
        Projeto projeto = new Projeto();
        projeto.setId(7L);
        Usuario avaliador = new Usuario();
        avaliador.setId(20L);

        Atribuicao nova = new Atribuicao();
        nova.setId(99L);
        nova.setProjeto(projeto);
        nova.setAvaliador(avaliador);
        nova.setStatus(AtribuicaoStatus.ATIVA);
        nova.setOrigem(AtribuicaoOrigem.MANUAL);

        when(service.reatribuir(eq(50L), eq(20L), eq("Avaliador indisponível"), eq(1L))).thenReturn(nova);

        String json = objectMapper.writeValueAsString(Map.of("novoAvaliadorId", 20, "motivo", "Avaliador indisponível"));

        mvc.perform(post(API + "/50/reatribuir").with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(99))
            .andExpect(jsonPath("$.avaliadorId").value(20))
            .andExpect(jsonPath("$.origem").value("MANUAL"));
    }

    @Test
    void reatribuirSemMotivoRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("novoAvaliadorId", 20, "motivo", ""));

        mvc.perform(post(API + "/50/reatribuir").with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }
}
