package com.avaliacao.sistema.service;

import com.avaliacao.sistema.entity.Campus;
import com.avaliacao.sistema.entity.Comite;
import com.avaliacao.sistema.entity.Instituicao;
import com.avaliacao.sistema.repository.CampusAliasRepository;
import com.avaliacao.sistema.repository.CampusRepository;
import com.avaliacao.sistema.repository.ComiteAliasRepository;
import com.avaliacao.sistema.repository.ComiteRepository;
import com.avaliacao.sistema.repository.InstituicaoRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ponto único de "buscar ou criar" comitê/campus/instituição a partir de um nome digitado — usado no
 * cadastro de avaliador, de projeto e no vínculo a uma edição, pra evitar que o mesmo texto normalizado
 * de formas diferentes crie registros duplicados dependendo de qual caminho rodou primeiro.
 */
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final ComiteRepository comiteRepository;
    private final ComiteAliasRepository comiteAliasRepository;
    private final CampusRepository campusRepository;
    private final CampusAliasRepository campusAliasRepository;
    private final InstituicaoRepository instituicaoRepository;

    /** Primeiro tenta um alias conhecido (ex.: nome como vem do Sigaa); se não achar, resolve pelo nome direto. */
    @Transactional
    public Comite resolverComite(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String normalizado = nome.trim();
        return comiteAliasRepository.findByTextoOrigemIgnoreCase(normalizado)
            .map(alias -> alias.getComite())
            .filter(Objects::nonNull)
            .orElseGet(() -> comiteRepository.findByNomeIgnoreCase(normalizado)
                .orElseGet(() -> {
                    Comite novo = new Comite();
                    novo.setNome(normalizado);
                    return comiteRepository.save(novo);
                }));
    }

    /** Mesmo padrão de resolverComite, pro campus (local físico do evento). */
    @Transactional
    public Campus resolverCampus(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String normalizado = nome.trim();
        return campusAliasRepository.findByTextoOrigemIgnoreCase(normalizado)
            .map(alias -> alias.getCampus())
            .filter(Objects::nonNull)
            .orElseGet(() -> campusRepository.findByNomeIgnoreCase(normalizado)
                .orElseGet(() -> {
                    Campus novo = new Campus();
                    novo.setNome(normalizado);
                    return campusRepository.save(novo);
                }));
    }

    @Transactional
    public Instituicao resolverInstituicao(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        String normalizado = nome.trim();
        return instituicaoRepository.findByNomeIgnoreCase(normalizado)
            .orElseGet(() -> {
                Instituicao nova = new Instituicao();
                nova.setNome(normalizado);
                return instituicaoRepository.save(nova);
            });
    }
}
