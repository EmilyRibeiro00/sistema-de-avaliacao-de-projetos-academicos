-- Cópia idêntica de V9 (ver backend/README.md) — nenhuma das instruções é Postgres-específica.
ALTER TABLE usuario ADD COLUMN precisa_trocar_senha BOOLEAN NOT NULL DEFAULT false;

UPDATE usuario SET senha_hash = '$2a$10$DU.9wn0cm0HiXjJTxvL3Uu8jSAXPd/UZdsYuuGwNl6ckAPKKBY.Ni', precisa_trocar_senha = true
WHERE senha_hash IS NULL;

ALTER TABLE usuario DROP COLUMN token_ativacao;
ALTER TABLE usuario DROP COLUMN token_ativacao_expira_em;
