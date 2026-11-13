package com.avaliacao.sistema.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Pessoa;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.entity.enums.UsuarioStatusAtivacao;
import com.avaliacao.sistema.entity.enums.UsuarioTipo;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

// Roda contra H2 (perfil "test", ver application-test.yml) pra checar o mapeamento sem depender de Postgres
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioRepositoryTest {

    @Autowired UsuarioRepository repository;
    @Autowired PessoaRepository pessoaRepository;
    @Autowired ComiteRepository comiteRepository;

    private Pessoa pessoa(String cpf, String email) {
        Pessoa p = new Pessoa();
        p.setNome("Teste");
        p.setCpf(cpf);
        p.setEmail(email);
        return pessoaRepository.save(p);
    }

    // Avaliador ATIVO exige comitê definido (chk_usuario_avaliador_comite)
    private Comite comite() {
        Comite c = new Comite();
        c.setNome("Comitê de Teste " + System.nanoTime());
        return comiteRepository.save(c);
    }

    @Test
    void deveVerificarSalvarUsuario() {
        Pessoa pessoa = pessoa("11111111101", "teste1@teste.com");

        Usuario user = new Usuario();
        user.setPessoa(pessoa);
        user.setTipo(UsuarioTipo.AVALIADOR);
        user.setStatusAtivacao(UsuarioStatusAtivacao.ATIVO);
        user.setComite(comite());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        Usuario salvo = repository.save(user);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getPessoa().getEmail()).isEqualTo("teste1@teste.com");
        assertThat(salvo.getTipo()).isEqualTo(UsuarioTipo.AVALIADOR);
    }

    @Test
    void deveVerificarRemoverUsuario() {
        Pessoa pessoa = pessoa("11111111102", "teste2@teste.com");
        Usuario user = new Usuario();
        user.setPessoa(pessoa);
        user.setTipo(UsuarioTipo.AVALIADOR);
        user.setStatusAtivacao(UsuarioStatusAtivacao.ATIVO);
        user.setComite(comite());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        Usuario salvo = repository.save(user);
        Long id = salvo.getId();
        repository.deleteById(id);

        Optional<Usuario> temp = repository.findById(id);
        assertThat(temp).isNotPresent();
    }

    @Test
    void findByPessoaEmailEncontraOUsuarioPeloEmailDaPessoa() {
        Pessoa pessoa = pessoa("11111111103", "busca@teste.com");
        Usuario user = new Usuario();
        user.setPessoa(pessoa);
        user.setTipo(UsuarioTipo.ADMIN);
        user.setStatusAtivacao(UsuarioStatusAtivacao.ATIVO);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        repository.save(user);

        Optional<Usuario> encontrado = repository.findByPessoaEmail("busca@teste.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTipo()).isEqualTo(UsuarioTipo.ADMIN);
    }

    @Test
    void cadastroPendenteDeAtivacaoPersisteComSenhaPadraoEFlagDeTroca() {
        Pessoa pessoa = pessoa("11111111104", "pendente@teste.com");
        Usuario user = new Usuario();
        user.setPessoa(pessoa);
        user.setTipo(UsuarioTipo.AVALIADOR);
        user.setStatusAtivacao(UsuarioStatusAtivacao.PENDENTE_ATIVACAO);
        user.setSenhaHash("hash-bcrypt");
        user.setPrecisaTrocarSenha(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        Usuario salvo = repository.save(user);

        Optional<Usuario> encontrado = repository.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getStatusAtivacao()).isEqualTo(UsuarioStatusAtivacao.PENDENTE_ATIVACAO);
        assertThat(encontrado.get().getPrecisaTrocarSenha()).isTrue();
    }
}
