-- Renomeia área de conhecimento para comitê: cada projeto responde a um comitê só (1:1 com o que
-- hoje é "área"), e o evento chama isso de comitê, não de área.
ALTER TABLE area_conhecimento RENAME TO comite;
ALTER TABLE comite RENAME COLUMN area_pai_id TO comite_pai_id;

ALTER TABLE avaliador_area RENAME TO avaliador_comite;
ALTER TABLE avaliador_comite RENAME COLUMN area_id TO comite_id;
ALTER TABLE avaliador_comite RENAME CONSTRAINT uk_avaliador_area TO uk_avaliador_comite;

ALTER TABLE area_conhecimento_alias RENAME TO comite_alias;
ALTER TABLE comite_alias RENAME COLUMN area_id TO comite_id;
ALTER TABLE comite_alias RENAME CONSTRAINT uk_area_alias_texto_origem TO uk_comite_alias_texto_origem;

ALTER TABLE projeto RENAME COLUMN area_id TO comite_id;

-- Local (campus) do evento: o projeto tem um local de origem, o avaliador tem um local de atuação
-- por edição (igual já acontece com o comitê, no vínculo avaliador-edição).
CREATE TABLE local (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sigla VARCHAR(50)
);

ALTER TABLE projeto ADD COLUMN local_id BIGINT REFERENCES local(id);
ALTER TABLE edicao_avaliador ADD COLUMN local_id BIGINT REFERENCES local(id);
