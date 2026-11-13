-- Suporte a remoção lógica (soft delete) em Critério, Projeto e Edição: "remover" passa a marcar
-- ativo=false em vez de apagar a linha. Avaliador (Usuario) reaproveita status_ativacao=INATIVO, que
-- já existia; EdicaoAvaliador reaproveita status=INATIVO também — nenhum dos dois precisa de coluna nova.
ALTER TABLE criterio ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE projeto ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE edicao ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT true;
