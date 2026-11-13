-- "local" (V3) foi nome provisório enquanto decidíamos a modelagem; o nome final é "campus" —
-- mais específico, já que "instituição" continua existindo separada (a universidade/organização)
-- e "campus" é especificamente o local físico do evento dentro da UFMA.
ALTER TABLE local RENAME TO campus;
ALTER TABLE projeto RENAME COLUMN local_id TO campus_id;
ALTER TABLE edicao_avaliador RENAME COLUMN local_id TO campus_id;
