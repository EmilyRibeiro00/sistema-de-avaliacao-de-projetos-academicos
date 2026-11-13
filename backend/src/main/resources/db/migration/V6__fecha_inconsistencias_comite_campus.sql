-- Fecha os 5 pontos da segunda revisão de inconsistências.

-- 1) As UNIQUE criadas na V5 eram case-sensitive, mas CatalogoService resolve por
-- findByNomeIgnoreCase: duas requisições concorrentes com caixas diferentes ainda
-- conseguiam duplicar o mesmo nome. Troca por índice único sobre lower(nome).
ALTER TABLE comite DROP CONSTRAINT uk_comite_nome;
ALTER TABLE campus DROP CONSTRAINT uk_campus_nome;
ALTER TABLE instituicao DROP CONSTRAINT uk_instituicao_nome;
CREATE UNIQUE INDEX uk_comite_nome ON comite (lower(nome));
CREATE UNIQUE INDEX uk_campus_nome ON campus (lower(nome));
CREATE UNIQUE INDEX uk_instituicao_nome ON instituicao (lower(nome));

-- 2) comite_pai_id (herdado de area_pai_id) não tem nenhum uso no código e não há
-- hierarquia confirmada para comitê de avaliação. Remove a coluna morta.
ALTER TABLE comite DROP COLUMN comite_pai_id;

-- 3a) Garante que todo avaliador ATIVO tenha comitê definido (fica livre durante
-- PENDENTE_ATIVACAO/INATIVO, para não travar importação antes da resolução do comitê).
ALTER TABLE usuario ADD CONSTRAINT chk_usuario_avaliador_comite
    CHECK (tipo <> 'AVALIADOR' OR status_ativacao <> 'ATIVO' OR comite_id IS NOT NULL);

-- 4) comite não tinha sigla, diferente de instituicao e campus.
ALTER TABLE comite ADD COLUMN sigla VARCHAR(50);

-- 5) Equivalente a comite_alias, para resolver strings cruas de campus vindas de importação.
CREATE TABLE campus_alias (
    id BIGSERIAL PRIMARY KEY,
    texto_origem VARCHAR(255) NOT NULL,
    campus_id BIGINT REFERENCES campus(id),
    CONSTRAINT uk_campus_alias_texto_origem UNIQUE (texto_origem)
);
