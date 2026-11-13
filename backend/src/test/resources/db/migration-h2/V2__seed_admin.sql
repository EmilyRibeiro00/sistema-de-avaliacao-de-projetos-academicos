-- Cópia de V2__seed_admin.sql adaptada pro H2: setval/pg_get_serial_sequence são específicos do Postgres,
-- viram ALTER TABLE ... RESTART WITH no H2.

INSERT INTO pessoa (id, nome, cpf, email) VALUES
    (1, 'Coordenação Geral', '00000000000', 'admin@instituicao.edu.br');

INSERT INTO usuario (id, pessoa_id, senha_hash, tipo, status_ativacao, created_at, updated_at) VALUES
    (1, 1, '$2b$10$yD.8sUzARYVfU1CHfJ8fveNFPXiPYnBNUMzCkxZeQTmbzcmopPhpa', 'ADMIN', 'ATIVO', now(), now());

ALTER TABLE pessoa ALTER COLUMN id RESTART WITH 2;
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 2;
