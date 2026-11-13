-- Seed mínimo de dev/local: só o suficiente para o primeiro login existir.
-- Sem avaliadores, projetos, edições ou critérios de exemplo de propósito — na vida real,
-- avaliadores só entram por importação de arquivo e projetos só por importação do Sigaa
-- (ver docs/plano-implementacao.md); tudo isso fica para o admin configurar/importar de verdade,
-- não para dado fictício fingindo que já existe.

INSERT INTO pessoa (id, nome, cpf, email) VALUES
    (1, 'Coordenação Geral', '00000000000', 'admin@instituicao.edu.br');

-- senha_hash gerado com BCrypt (custo 10) para "admin123"
INSERT INTO usuario (id, pessoa_id, senha_hash, tipo, status_ativacao, created_at, updated_at) VALUES
    (1, 1, '$2b$10$yD.8sUzARYVfU1CHfJ8fveNFPXiPYnBNUMzCkxZeQTmbzcmopPhpa', 'ADMIN', 'ATIVO', now(), now());

SELECT setval(pg_get_serial_sequence('pessoa', 'id'), (SELECT MAX(id) FROM pessoa));
SELECT setval(pg_get_serial_sequence('usuario', 'id'), (SELECT MAX(id) FROM usuario));
