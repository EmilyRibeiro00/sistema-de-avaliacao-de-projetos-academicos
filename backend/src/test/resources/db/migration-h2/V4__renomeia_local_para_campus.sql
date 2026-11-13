-- Cópia idêntica de V4 para H2 (ver backend/README.md) — nenhuma instrução aqui usa TIMESTAMPTZ
-- nem índice único parcial.
ALTER TABLE local RENAME TO campus;
ALTER TABLE projeto RENAME COLUMN local_id TO campus_id;
ALTER TABLE edicao_avaliador RENAME COLUMN local_id TO campus_id;
