package com.avaliacao.sistema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.avaliacao.sistema.entity.LogAuditoria;
import com.avaliacao.sistema.entity.Usuario;
import com.avaliacao.sistema.repository.LogAuditoriaRepository;
import com.avaliacao.sistema.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditoriaServiceTest {

    @Mock LogAuditoriaRepository logAuditoriaRepository;
    @Mock UsuarioRepository usuarioRepository;

    AuditoriaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-01T12:00:00Z"), ZoneOffset.UTC);
        service = new AuditoriaService(logAuditoriaRepository, usuarioRepository, clock);
    }

    @Test
    void registraUsuarioAcaoEntidadeDetalhesEDataHora() {
        Usuario admin = TestData.avaliador(9);
        when(usuarioRepository.getReferenceById(9L)).thenReturn(admin);

        service.registrar(9L, "EDICAO_CRIADA", "Edicao", 1L, "Mostra 2026");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).save(captor.capture());
        LogAuditoria log = captor.getValue();
        assertThat(log.getUsuario()).isSameAs(admin);
        assertThat(log.getAcao()).isEqualTo("EDICAO_CRIADA");
        assertThat(log.getEntidade()).isEqualTo("Edicao");
        assertThat(log.getEntidadeId()).isEqualTo(1L);
        assertThat(log.getDetalhes()).isEqualTo("Mostra 2026");
        assertThat(log.getDataHora()).isEqualTo(Instant.parse("2026-10-01T12:00:00Z"));
    }

    @Test
    void acaoDoSistemaFicaSemUsuario() {
        service.registrar(null, "ATRIBUICAO_AUTOMATICA", "Atribuicao", 5L, "detalhe");

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(logAuditoriaRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isNull();
        verify(usuarioRepository, never()).getReferenceById(org.mockito.ArgumentMatchers.anyLong());
    }
}
