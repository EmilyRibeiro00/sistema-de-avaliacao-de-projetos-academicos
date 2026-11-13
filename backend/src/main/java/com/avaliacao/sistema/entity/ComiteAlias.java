package com.avaliacao.sistema.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "comite_alias",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_comite_alias_texto_origem", columnNames = "texto_origem")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class ComiteAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String textoOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comite_id")
    private Comite comite;
}
