-- Cópia adaptada de V5 para H2 (ver backend/README.md). A parte de dados/constraints é idêntica à
-- de produção; a parte final de V5 (renomear sequence/constraint que ficaram com nome antigo) é
-- Postgres-específica — o H2, nesse modo, usa nomes de constraint opacos (CONSTRAINT_xxx) e não usa
-- sequence nomeada para BIGSERIAL, então não há nada para renomear aqui.
ALTER TABLE usuario ADD COLUMN comite_id BIGINT REFERENCES comite(id);
UPDATE usuario u SET comite_id = ac.comite_id FROM avaliador_comite ac WHERE ac.avaliador_id = u.id;
DROP TABLE avaliador_comite;

ALTER TABLE comite ADD CONSTRAINT uk_comite_nome UNIQUE (nome);
ALTER TABLE campus ADD CONSTRAINT uk_campus_nome UNIQUE (nome);
ALTER TABLE instituicao ADD CONSTRAINT uk_instituicao_nome UNIQUE (nome);
ALTER TABLE pessoa ADD CONSTRAINT uk_pessoa_email UNIQUE (email);
