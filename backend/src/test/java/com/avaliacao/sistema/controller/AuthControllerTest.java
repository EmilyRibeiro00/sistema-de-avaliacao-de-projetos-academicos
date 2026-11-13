package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.controller.dto.LoginResponse;
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

@WebMvcTest(controllers = AuthController.class)
@Import(WebSecurityTestConfig.class)
class AuthControllerTest {

    static final String API = "/auth";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService service;

    @Test
    void loginComCredenciaisValidasRetornaTokenEnTipo() throws Exception {
        when(service.login(anyString(), anyString())).thenReturn(new LoginResponse("jwt-abc", "ADMIN"));

        String json = objectMapper.writeValueAsString(Map.of("identificador", "admin@teste.br", "senha", "admin123"));

        mvc.perform(post(API + "/login").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-abc"))
            .andExpect(jsonPath("$.tipo").value("ADMIN"));
    }

    @Test
    void loginSemSenhaRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("identificador", "admin@teste.br", "senha", ""));

        mvc.perform(post(API + "/login").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());
    }
}
