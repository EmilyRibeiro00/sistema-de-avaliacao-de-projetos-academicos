-- Cópia idêntica de V8 para H2 (ver backend/README.md) — ALTER TABLE ... ADD COLUMN simples, sem
-- nada específico de Postgres.
ALTER TABLE criterio ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE projeto ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE edicao ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
