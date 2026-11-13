package com.avaliacao.sistema.entity;

import com.avaliacao.sistema.entity.enums.ImportacaoLoteOrigem;
import com.avaliacao.sistema.entity.enums.ImportacaoLoteStatus;
import com.avaliacao.sistema.entity.enums.ImportacaoLoteTipo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "importacao_lote")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ImportacaoLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImportacaoLoteTipo tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edicao_id", nullable = false)
    private Edicao edicao;

    @Enumerated(EnumType.STRING)
    private ImportacaoLoteOrigem origem;

    private String payloadBrutoRef;

    @Enumerated(EnumType.STRING)
    private ImportacaoLoteStatus status;

    private Integer totalItens = 0;

    private Integer totalSucesso = 0;

    private Integer totalErro = 0;

    private Instant iniciadoEm;

    private Instant concluidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credencial_id")
    private IntegracaoCredencial credencial;
}
