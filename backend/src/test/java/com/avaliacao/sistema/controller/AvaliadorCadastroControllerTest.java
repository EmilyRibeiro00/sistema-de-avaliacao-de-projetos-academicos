package com.avaliacao.sistema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.EdicaoAvaliadorStatus;
import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import com.avaliacao.sistema.service.EdicaoAvaliadorService;
import com.avaliacao.sistema.service.UsuarioService;
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

@WebMvcTest(controllers = AvaliadorCadastroController.class)
@Import(WebSecurityTestConfig.class)
class AvaliadorCadastroControllerTest {

    static final String API = "/api/admin/avaliadores";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UsuarioService service;
    @MockBean EdicaoAvaliadorService edicaoAvaliadorService;

    RequestPostProcessor comoAdmin;
    RequestPostProcessor comoAvaliador;

    @BeforeEach
    void setUp() {
        comoAdmin = ControllerTestSupport.como(ControllerTestSupport.usuario(1, UsuarioTipo.ADMIN));
        comoAvaliador = ControllerTestSupport.como(ControllerTestSupport.usuario(2, UsuarioTipo.AVALIADOR));
    }

    private Usuario avaliadorFake() {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome("Maria Avaliadora");
        pessoa.setEmail("maria@instituicao.edu.br");

        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setPessoa(pessoa);
        usuario.setTipo(UsuarioTipo.AVALIADOR);
        usuario.setStatusAtivacao(UsuarioStatusAtivacao.PENDENTE_ATIVACAO);
        usuario.setPrecisaTrocarSenha(true);
        return usuario;
    }

    private String jsonValido() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "nome", "Maria Avaliadora",
            "email", "maria@instituicao.edu.br",
            "cpf", "11122233344"));
    }

    @Test
    void cadastrarComoAdminRetorna201() throws Exception {
        when(service.cadastrarAvaliador(any(), eq(1L))).thenReturn(avaliadorFake());
        when(service.getSenhaPadraoAvaliador()).thenReturn("Trocar@123");

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(jsonValido()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.email").value("maria@instituicao.edu.br"))
            .andExpect(jsonPath("$.senhaTemporaria").value("Trocar@123"));
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
    void cadastrarSemNomeRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "nome", "",
            "email", "maria@instituicao.edu.br",
            "cpf", "11122233344"));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void cadastrarComEmailInvalidoRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of(
            "nome", "Maria Avaliadora",
            "email", "nao-e-um-email",
            "cpf", "11122233344"));

        mvc.perform(post(API).with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    private EdicaoAvaliador participacaoFake() {
        Edicao edicao = new Edicao();
        edicao.setId(10L);

        Comite comite = new Comite();
        comite.setId(1L);
        comite.setNome("Comitê de Tecnologia");

        Campus campus = new Campus();
        campus.setId(1L);
        campus.setNome("Campus Bacanga");

        Usuario avaliador = avaliadorFake();
        avaliador.setComite(comite);

        EdicaoAvaliador participacao = new EdicaoAvaliador();
        participacao.setEdicao(edicao);
        participacao.setAvaliador(avaliador);
        participacao.setStatus(EdicaoAvaliadorStatus.ATIVO);
        participacao.setCampus(campus);
        return participacao;
    }

    private String jsonVinculo() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
            "edicaoId", 10, "comiteNome", "Comitê de Tecnologia", "campusNome", "Campus Bacanga"));
    }

    @Test
    void vincularComoAdminRetorna200() throws Exception {
        when(edicaoAvaliadorService.vincular(eq(7L), any(), eq(1L))).thenReturn(participacaoFake());

        mvc.perform(post(API + "/7/edicoes").with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(jsonVinculo()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.avaliadorId").value(7))
            .andExpect(jsonPath("$.edicaoId").value(10))
            .andExpect(jsonPath("$.status").value("ATIVO"))
            .andExpect(jsonPath("$.comiteNome").value("Comitê de Tecnologia"))
            .andExpect(jsonPath("$.campusNome").value("Campus Bacanga"));
    }

    @Test
    void vincularComoAvaliadorRetorna403() throws Exception {
        mvc.perform(post(API + "/7/edicoes").with(comoAvaliador).contentType(MediaType.APPLICATION_JSON).content(jsonVinculo()))
            .andExpect(status().isForbidden());
    }

    @Test
    void vincularSemEdicaoIdRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("comiteNome", "Comitê de Tecnologia", "campusNome", "Campus Bacanga"));

        mvc.perform(post(API + "/7/edicoes").with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    void vincularSemComiteNomeRetorna400() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("edicaoId", 10, "campusNome", "Campus Bacanga"));

        mvc.perform(post(API + "/7/edicoes").with(comoAdmin).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest());
    }
}
