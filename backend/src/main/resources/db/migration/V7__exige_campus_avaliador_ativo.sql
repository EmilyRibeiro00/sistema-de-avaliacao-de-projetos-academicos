-- Garante que todo vínculo avaliador-edição ATIVO tenha campus definido (fica livre em INATIVO,
-- para não travar importação antes da resolução do campus). Os dois vínculos de teste que já
-- existiam sem campus foram corrigidos manualmente antes desta migration.
ALTER TABLE edicao_avaliador ADD CONSTRAINT chk_edicao_avaliador_ativo_campus
    CHECK (status <> 'ATIVO' OR campus_id IS NOT NULL);
