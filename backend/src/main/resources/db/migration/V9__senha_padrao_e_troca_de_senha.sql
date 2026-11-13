-- Troca o fluxo de ativação por link/token por senha padrão + troca obrigatória no primeiro acesso:
-- o avaliador já nasce com senha (a padrão) e loga direto; precisa_trocar_senha sinaliza que ele
-- ainda está usando a senha padrão e precisa definir uma própria.
ALTER TABLE usuario ADD COLUMN precisa_trocar_senha BOOLEAN NOT NULL DEFAULT false;

-- Backfill: quem ainda estava sem senha (fluxo antigo, nunca ativou pelo link) recebe a senha padrão
-- e fica marcado para trocar no próximo login. Hash de "Trocar@123" (bcrypt, custo 10), mesmo padrão
-- já usado pelo admin semeado na V2.
UPDATE usuario SET senha_hash = '$2a$10$DU.9wn0cm0HiXjJTxvL3Uu8jSAXPd/UZdsYuuGwNl6ckAPKKBY.Ni', precisa_trocar_senha = true
WHERE senha_hash IS NULL;

ALTER TABLE usuario DROP COLUMN token_ativacao;
ALTER TABLE usuario DROP COLUMN token_ativacao_expira_em;
