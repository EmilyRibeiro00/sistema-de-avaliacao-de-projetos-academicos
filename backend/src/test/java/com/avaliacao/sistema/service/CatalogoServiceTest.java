package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.CampusAlias;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.ComiteAlias;
import com.avaliacao.sistema.entity.Instituicao;
import com.avaliacao.sistema.repository.CampusAliasRepository;
import com.avaliacao.sistema.repository.CampusRepository;
import com.avaliacao.sistema.repository.ComiteAliasRepository;
import com.avaliacao.sistema.repository.ComiteRepository;
import com.avaliacao.sistema.repository.InstituicaoRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogoServiceTest {

    @Mock ComiteRepository comiteRepository;
    @Mock ComiteAliasRepository comiteAliasRepository;
    @Mock CampusRepository campusRepository;
    @Mock CampusAliasRepository campusAliasRepository;
    @Mock InstituicaoRepository instituicaoRepository;

    CatalogoService service;

    @BeforeEach
    void setUp() {
        service = new CatalogoService(comiteRepository, comiteAliasRepository, campusRepository, campusAliasRepository, instituicaoRepository);
        when(comiteAliasRepository.findByTextoOrigemIgnoreCase(any())).thenReturn(Optional.empty());
        when(comiteRepository.save(any(Comite.class))).thenAnswer(i -> i.getArgument(0));
        when(campusAliasRepository.findByTextoOrigemIgnoreCase(any())).thenReturn(Optional.empty());
        when(campusRepository.save(any(Campus.class))).thenAnswer(i -> i.getArgument(0));
        when(instituicaoRepository.save(any(Instituicao.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void resolverComiteRetornaNullSemNome() {
        assertThat(service.resolverComite(null)).isNull();
        assertThat(service.resolverComite("  ")).isNull();
    }

    @Test
    void resolverComiteUsaAliasQuandoExisteComComiteAssociado() {
        Comite comite = TestData.comite(5L);
        ComiteAlias alias = new ComiteAlias();
        alias.setTextoOrigem("Ciência da Computação");
        alias.setComite(comite);
        when(comiteAliasRepository.findByTextoOrigemIgnoreCase("Ciência da Computação"))
            .thenReturn(Optional.of(alias));

        Comite resolvido = service.resolverComite("Ciência da Computação");

        assertThat(resolvido).isSameAs(comite);
        verify(comiteRepository, never()).findByNomeIgnoreCase(any());
    }

    @Test
    void resolverComiteIgnoraAliasSemComiteAssociadoEBuscaPorNome() {
        ComiteAlias aliasSemComite = new ComiteAlias();
        aliasSemComite.setTextoOrigem("Texto pendente de mapeamento");
        when(comiteAliasRepository.findByTextoOrigemIgnoreCase("Texto pendente de mapeamento"))
            .thenReturn(Optional.of(aliasSemComite));
        when(comiteRepository.findByNomeIgnoreCase("Texto pendente de mapeamento")).thenReturn(Optional.empty());

        Comite resolvido = service.resolverComite("Texto pendente de mapeamento");

        assertThat(resolvido.getNome()).isEqualTo("Texto pendente de mapeamento");
    }

    @Test
    void resolverComiteCriaQuandoNaoExisteNemComoAliasNemComoComite() {
        when(comiteRepository.findByNomeIgnoreCase("Computação")).thenReturn(Optional.empty());

        Comite criado = service.resolverComite(" Computação ");

        assertThat(criado.getNome()).isEqualTo("Computação");
    }

    @Test
    void resolverComiteReaproveitaExistentePeloNomeSemDuplicar() {
        Comite existente = TestData.comite(9L);
        when(comiteRepository.findByNomeIgnoreCase("Comitê 9")).thenReturn(Optional.of(existente));

        Comite resolvido = service.resolverComite("Comitê 9");

        assertThat(resolvido).isSameAs(existente);
        verify(comiteRepository, never()).save(any());
    }

    @Test
    void resolverCampusRetornaNullSemNome() {
        assertThat(service.resolverCampus(null)).isNull();
        assertThat(service.resolverCampus("  ")).isNull();
    }

    @Test
    void resolverCampusUsaAliasQuandoExisteComCampusAssociado() {
        Campus campus = TestData.campus(5L);
        CampusAlias alias = new CampusAlias();
        alias.setTextoOrigem("Dom Delgado");
        alias.setCampus(campus);
        when(campusAliasRepository.findByTextoOrigemIgnoreCase("Dom Delgado")).thenReturn(Optional.of(alias));

        Campus resolvido = service.resolverCampus("Dom Delgado");

        assertThat(resolvido).isSameAs(campus);
        verify(campusRepository, never()).findByNomeIgnoreCase(any());
    }

    @Test
    void resolverCampusCriaQuandoNaoExisteNemComoAliasNemComoCampus() {
        when(campusRepository.findByNomeIgnoreCase("Campus Bacanga")).thenReturn(Optional.empty());

        Campus criado = service.resolverCampus(" Campus Bacanga ");

        assertThat(criado.getNome()).isEqualTo("Campus Bacanga");
    }

    @Test
    void resolverCampusReaproveitaExistentePeloNomeSemDuplicar() {
        Campus existente = TestData.campus(9L);
        when(campusRepository.findByNomeIgnoreCase("Campus 9")).thenReturn(Optional.of(existente));

        Campus resolvido = service.resolverCampus("Campus 9");

        assertThat(resolvido).isSameAs(existente);
        verify(campusRepository, never()).save(any());
    }

    @Test
    void resolverInstituicaoRetornaNullSemNome() {
        assertThat(service.resolverInstituicao(null)).isNull();
        assertThat(service.resolverInstituicao("")).isNull();
    }

    @Test
    void resolverInstituicaoCriaQuandoNaoExiste() {
        when(instituicaoRepository.findByNomeIgnoreCase("UFXX")).thenReturn(Optional.empty());

        Instituicao criada = service.resolverInstituicao("UFXX");

        assertThat(criada.getNome()).isEqualTo("UFXX");
    }

    @Test
    void resolverInstituicaoReaproveitaExistenteSemDuplicar() {
        Instituicao existente = new Instituicao();
        existente.setId(3L);
        existente.setNome("UFXX");
        when(instituicaoRepository.findByNomeIgnoreCase("UFXX")).thenReturn(Optional.of(existente));

        Instituicao resolvida = service.resolverInstituicao("UFXX");

        assertThat(resolvida).isSameAs(existente);
        verify(instituicaoRepository, never()).save(any());
    }
}
