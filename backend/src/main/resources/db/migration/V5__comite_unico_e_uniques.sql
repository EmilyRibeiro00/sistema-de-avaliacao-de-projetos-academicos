-- Fecha a inconsistência de escopo encontrada na revisão: avaliador só responde por um comitê, e
-- esse comitê é fixo (não varia por edição, diferente de campus). comite_id passa a ser atributo
-- direto de usuario, igual já é em projeto; a tabela avaliador_comite (N:N, sem escopo de edição)
-- deixa de existir. Os vínculos existentes são preservados antes do DROP.
ALTER TABLE usuario ADD COLUMN comite_id BIGINT REFERENCES comite(id);
UPDATE usuario u SET comite_id = ac.comite_id FROM avaliador_comite ac WHERE ac.avaliador_id = u.id;
DROP TABLE avaliador_comite;

-- Garante unicidade de nome nos catálogos resolvidos por nome (CatalogoService busca-ou-cria por
-- nome; sem UNIQUE, requisições concorrentes podem duplicar a mesma linha).
ALTER TABLE comite ADD CONSTRAINT uk_comite_nome UNIQUE (nome);
ALTER TABLE campus ADD CONSTRAINT uk_campus_nome UNIQUE (nome);
ALTER TABLE instituicao ADD CONSTRAINT uk_instituicao_nome UNIQUE (nome);
ALTER TABLE pessoa ADD CONSTRAINT uk_pessoa_email UNIQUE (email);

-- Corrige nomes de constraint/sequence que ficaram com o nome antigo (area/local) depois dos renames
-- feitos em V3/V4 — cosmético, não afeta comportamento, só deixa "\d" legível no psql.
ALTER SEQUENCE area_conhecimento_id_seq RENAME TO comite_id_seq;
ALTER TABLE comite RENAME CONSTRAINT area_conhecimento_pkey TO comite_pkey;
ALTER TABLE comite RENAME CONSTRAINT area_conhecimento_area_pai_id_fkey TO comite_comite_pai_id_fkey;

ALTER SEQUENCE area_conhecimento_alias_id_seq RENAME TO comite_alias_id_seq;
ALTER TABLE comite_alias RENAME CONSTRAINT area_conhecimento_alias_pkey TO comite_alias_pkey;
ALTER TABLE comite_alias RENAME CONSTRAINT area_conhecimento_alias_area_id_fkey TO comite_alias_comite_id_fkey;

ALTER SEQUENCE local_id_seq RENAME TO campus_id_seq;
ALTER TABLE campus RENAME CONSTRAINT local_pkey TO campus_pkey;

ALTER TABLE projeto RENAME CONSTRAINT projeto_area_id_fkey TO projeto_comite_id_fkey;
ALTER TABLE projeto RENAME CONSTRAINT projeto_local_id_fkey TO projeto_campus_id_fkey;

ALTER TABLE edicao_avaliador RENAME CONSTRAINT edicao_avaliador_local_id_fkey TO edicao_avaliador_campus_id_fkey;
