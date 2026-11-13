-- Cópia adaptada de V3 para H2 (ver backend/README.md). Nenhuma das instruções abaixo usa
-- TIMESTAMPTZ nem índice único parcial, então é idêntica à migration de produção.
ALTER TABLE area_conhecimento RENAME TO comite;
ALTER TABLE comite RENAME COLUMN area_pai_id TO comite_pai_id;

ALTER TABLE avaliador_area RENAME TO avaliador_comite;
ALTER TABLE avaliador_comite RENAME COLUMN area_id TO comite_id;
ALTER TABLE avaliador_comite RENAME CONSTRAINT uk_avaliador_area TO uk_avaliador_comite;

ALTER TABLE area_conhecimento_alias RENAME TO comite_alias;
ALTER TABLE comite_alias RENAME COLUMN area_id TO comite_id;
ALTER TABLE comite_alias RENAME CONSTRAINT uk_area_alias_texto_origem TO uk_comite_alias_texto_origem;

ALTER TABLE projeto RENAME COLUMN area_id TO comite_id;

CREATE TABLE local (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sigla VARCHAR(50)
);

ALTER TABLE projeto ADD COLUMN local_id BIGINT REFERENCES local(id);
ALTER TABLE edicao_avaliador ADD COLUMN local_id BIGINT REFERENCES local(id);
