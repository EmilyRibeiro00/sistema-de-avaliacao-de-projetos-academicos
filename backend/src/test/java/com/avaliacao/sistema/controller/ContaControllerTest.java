package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.controller.dto.LoginResponse;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(controllers = ContaController.class)
@Import(WebSecurityTestConfig.class)
class ContaControllerTest {

    static final String API = "/api/conta/senha";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService service;

    RequestPostProcessor autenticado = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.AVALIADOR));

    private String json(String senhaAtual, String novaSenha) throws Exception {
        return objectMapper.writeValueAsString(Map.of("senhaAtual", senhaAtual, "novaSenha", novaSenha));
    }

    @Test
    void trocarSenhaComDadosValidosRetornaNovoToken() throws Exception {
        when(service.trocarSenha(anyLong(), anyString(), anyString())).thenReturn(new LoginResponse("jwt-novo", "AVALIADOR"));

        mvc.perform(put(API).with(autenticado).contentType(MediaType.APPLICATION_JSON).content(json("atual123", "novaSenha123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-novo"));
    }

    @Test
    void trocarSenhaSemAutenticacaoRetorna401() throws Exception {
        mvc.perform(put(API).contentType(MediaType.APPLICATION_JSON).content(json("atual123", "novaSenha123")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void trocarSenhaComNovaSenhaCurtaRetorna400() throws Exception {
        mvc.perform(put(API).with(autenticado).contentType(MediaType.APPLICATION_JSON).content(json("atual123", "curta")))
            .andExpect(status().isBadRequest());
    }
}
