-- Cópia idêntica de V7 para H2 (ver backend/README.md) — CHECK simples, sem sintaxe específica
-- de Postgres.
ALTER TABLE edicao_avaliador ADD CONSTRAINT chk_edicao_avaliador_ativo_campus
    CHECK (status <> 'ATIVO' OR campus_id IS NOT NULL);
