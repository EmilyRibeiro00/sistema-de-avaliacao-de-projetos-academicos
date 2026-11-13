-- Cópia adaptada de V6 para H2 (ver backend/README.md). O item 1 (índice único sobre
-- lower(nome)) é Postgres-específico: este H2/modo não aceita expressões em CREATE INDEX,
-- então as UNIQUE (nome) simples criadas na V5 continuam como estão aqui — a correção de
-- case-insensitividade só existe no banco de produção/dev.
ALTER TABLE comite DROP COLUMN comite_pai_id;

ALTER TABLE usuario ADD CONSTRAINT chk_usuario_avaliador_comite
    CHECK (tipo <> 'AVALIADOR' OR status_ativacao <> 'ATIVO' OR comite_id IS NOT NULL);

ALTER TABLE comite ADD COLUMN sigla VARCHAR(50);

CREATE TABLE campus_alias (
    id BIGSERIAL PRIMARY KEY,
    texto_origem VARCHAR(255) NOT NULL,
    campus_id BIGINT REFERENCES campus(id),
    CONSTRAINT uk_campus_alias_texto_origem UNIQUE (texto_origem)
);
