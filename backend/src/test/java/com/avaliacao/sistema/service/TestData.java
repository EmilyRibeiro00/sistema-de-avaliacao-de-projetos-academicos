package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Atribuicao;
import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Criterio;
import com.avaliacao.sistema.entity.Edicao;
import com.avaliacao.sistema.entity.EdicaoAvaliador;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Projeto;
import com.avaliacao.sistema.entity.ProjetoDiscente;
import com.avaliacao.sistema.entity.ProjetoOrientador;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.AtribuicaoOrigem;
import com.avaliacao.sistema.entity.enums.AtribuicaoStatus;
import com.avaliacao.sistema.entity.enums.EdicaoAvaliadorStatus;
import com.avaliacao.sistema.entity.enums.EdicaoStatus;
import com.avaliacao.sistema.entity.enums.ProjetoDiscentePapel;
import com.avaliacao.sistema.entity.enums.ProjetoStatus;
import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import java.math.BigDecimal;
import java.time.Instant;

/** Fábricas de entidades em memória para os testes dos serviços. */
final class TestData {

    static final Instant ABERTURA = Instant.parse("2026-09-01T00:00:00Z");
    static final Instant FECHAMENTO = Instant.parse("2026-10-15T00:00:00Z");

    private TestData() {
    }

    static Pessoa pessoa(long id) {
        Pessoa p = new Pessoa();
        p.setId(id);
        p.setNome("Pessoa " + id);
        p.setEmail("pessoa" + id + "@teste.br");
        return p;
    }

    /** Usuário avaliador cujo id e cuja pessoa têm o mesmo número, para facilitar a leitura dos testes. */
    static Usuario avaliador(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setPessoa(pessoa(id));
        u.setTipo(UsuarioTipo.AVALIADOR);
        u.setStatusAtivacao(UsuarioStatusAtivacao.ATIVO);
        return u;
    }

    static Comite comite(long id) {
        Comite c = new Comite();
        c.setId(id);
        c.setNome("Comitê " + id);
        return c;
    }

    static Campus campus(long id) {
        Campus c = new Campus();
        c.setId(id);
        c.setNome("Campus " + id);
        return c;
    }

    static Edicao edicao(long id, EdicaoStatus status, int minimo) {
        Edicao e = new Edicao();
        e.setId(id);
        e.setNome("Edição " + id);
        e.setAno(2026);
        e.setStatus(status);
        e.setDataAbertura(ABERTURA);
        e.setDataFechamento(FECHAMENTO);
        e.setMinAvaliadoresPorProjeto(minimo);
        e.setEscalaNotaMin(BigDecimal.ZERO);
        e.setEscalaNotaMax(BigDecimal.TEN);
        return e;
    }

    static Projeto projeto(long id, Edicao edicao, Comite comite, Campus campus) {
        Projeto p = new Projeto();
        p.setId(id);
        p.setEdicao(edicao);
        p.setComite(comite);
        p.setCampus(campus);
        p.setTitulo("Projeto " + id);
        p.setConteudoTecnico("Conteúdo técnico " + id);
        p.setStatus(ProjetoStatus.SUBMETIDO);
        return p;
    }

    static Criterio criterio(long id, Edicao edicao, String nome, int peso) {
        Criterio c = new Criterio();
        c.setId(id);
        c.setEdicao(edicao);
        c.setNome(nome);
        c.setPeso(BigDecimal.valueOf(peso));
        return c;
    }

    static Atribuicao atribuicao(long id, Projeto projeto, Usuario avaliador, AtribuicaoStatus status) {
        Atribuicao a = new Atribuicao();
        a.setId(id);
        a.setProjeto(projeto);
        a.setAvaliador(avaliador);
        a.setStatus(status);
        a.setOrigem(AtribuicaoOrigem.AUTOMATICA);
        return a;
    }

    static EdicaoAvaliador participacao(Edicao edicao, Usuario avaliador, Integer limite, Campus campus) {
        EdicaoAvaliador ea = new EdicaoAvaliador();
        ea.setEdicao(edicao);
        ea.setAvaliador(avaliador);
        ea.setStatus(EdicaoAvaliadorStatus.ATIVO);
        ea.setLimiteMaxProjetos(limite);
        ea.setCampus(campus);
        return ea;
    }

    static ProjetoOrientador orientador(Projeto projeto, long pessoaId) {
        ProjetoOrientador o = new ProjetoOrientador();
        o.setProjeto(projeto);
        o.setPessoa(pessoa(pessoaId));
        return o;
    }

    static ProjetoDiscente discente(Projeto projeto, long pessoaId, ProjetoDiscentePapel papel) {
        ProjetoDiscente d = new ProjetoDiscente();
        d.setProjeto(projeto);
        d.setPessoa(pessoa(pessoaId));
        d.setPapel(papel);
        return d;
    }
}
