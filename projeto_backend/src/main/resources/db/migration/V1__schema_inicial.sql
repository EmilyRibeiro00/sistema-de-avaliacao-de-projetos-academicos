CREATE TABLE instituicao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    sigla VARCHAR(50)
);

CREATE TABLE pessoa (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14),
    matricula VARCHAR(50),
    siape VARCHAR(50),
    email VARCHAR(255),
    instituicao_id BIGINT REFERENCES instituicao(id),
    CONSTRAINT uk_pessoa_cpf UNIQUE (cpf)
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    pessoa_id BIGINT NOT NULL UNIQUE REFERENCES pessoa(id),
    senha_hash VARCHAR(255),
    tipo VARCHAR(20) NOT NULL,
    status_ativacao VARCHAR(20) NOT NULL,
    token_ativacao VARCHAR(255),
    token_ativacao_expira_em TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE area_conhecimento (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    area_pai_id BIGINT REFERENCES area_conhecimento(id)
);

CREATE TABLE avaliador_area (
    id BIGSERIAL PRIMARY KEY,
    avaliador_id BIGINT NOT NULL REFERENCES usuario(id),
    area_id BIGINT NOT NULL REFERENCES area_conhecimento(id),
    CONSTRAINT uk_avaliador_area UNIQUE (avaliador_id, area_id)
);

CREATE TABLE integracao_credencial (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    chave_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT true,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    ultimo_uso_em TIMESTAMPTZ
);

CREATE TABLE edicao (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    ano INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_abertura TIMESTAMPTZ,
    data_fechamento TIMESTAMPTZ,
    min_avaliadores_por_projeto INT NOT NULL DEFAULT 2,
    limite_max_projetos_por_avaliador INT,
    limite_divergencia_notas NUMERIC(4,2),
    escala_nota_min NUMERIC(4,2) NOT NULL DEFAULT 0,
    escala_nota_max NUMERIC(4,2) NOT NULL DEFAULT 10
);

CREATE TABLE criterio (
    id BIGSERIAL PRIMARY KEY,
    edicao_id BIGINT NOT NULL REFERENCES edicao(id),
    nome VARCHAR(255) NOT NULL,
    peso NUMERIC(5,2) NOT NULL,
    ordem INT NOT NULL DEFAULT 0
);

CREATE TABLE area_conhecimento_alias (
    id BIGSERIAL PRIMARY KEY,
    texto_origem VARCHAR(255) NOT NULL,
    area_id BIGINT REFERENCES area_conhecimento(id),
    CONSTRAINT uk_area_alias_texto_origem UNIQUE (texto_origem)
);

CREATE TABLE importacao_lote (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    edicao_id BIGINT NOT NULL REFERENCES edicao(id),
    origem VARCHAR(20) NOT NULL,
    payload_bruto_ref VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    total_itens INT NOT NULL DEFAULT 0,
    total_sucesso INT NOT NULL DEFAULT 0,
    total_erro INT NOT NULL DEFAULT 0,
    iniciado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    concluido_em TIMESTAMPTZ,
    usuario_id BIGINT REFERENCES usuario(id),
    credencial_id BIGINT REFERENCES integracao_credencial(id)
);

CREATE TABLE edicao_avaliador (
    id BIGSERIAL PRIMARY KEY,
    edicao_id BIGINT NOT NULL REFERENCES edicao(id),
    avaliador_id BIGINT NOT NULL REFERENCES usuario(id),
    status VARCHAR(20) NOT NULL,
    limite_max_projetos INT,
    importado_de BIGINT REFERENCES importacao_lote(id),
    CONSTRAINT uk_edicao_avaliador UNIQUE (edicao_id, avaliador_id)
);

CREATE TABLE projeto (
    id BIGSERIAL PRIMARY KEY,
    edicao_id BIGINT NOT NULL REFERENCES edicao(id),
    sigaa_id VARCHAR(100) NOT NULL,
    titulo VARCHAR(500) NOT NULL,
    conteudo_tecnico TEXT,
    area_id BIGINT REFERENCES area_conhecimento(id),
    instituicao_id BIGINT REFERENCES instituicao(id),
    status VARCHAR(20) NOT NULL,
    importado_de BIGINT REFERENCES importacao_lote(id),
    CONSTRAINT uk_projeto_sigaa_id_edicao UNIQUE (sigaa_id, edicao_id)
);

CREATE TABLE projeto_discente (
    id BIGSERIAL PRIMARY KEY,
    projeto_id BIGINT NOT NULL REFERENCES projeto(id),
    pessoa_id BIGINT NOT NULL REFERENCES pessoa(id),
    papel VARCHAR(20) NOT NULL
);

CREATE TABLE projeto_orientador (
    id BIGSERIAL PRIMARY KEY,
    projeto_id BIGINT NOT NULL REFERENCES projeto(id),
    pessoa_id BIGINT NOT NULL REFERENCES pessoa(id)
);

CREATE TABLE atribuicao (
    id BIGSERIAL PRIMARY KEY,
    projeto_id BIGINT NOT NULL REFERENCES projeto(id),
    avaliador_id BIGINT NOT NULL REFERENCES usuario(id),
    status VARCHAR(20) NOT NULL,
    origem VARCHAR(20) NOT NULL,
    motivo TEXT,
    criado_por BIGINT REFERENCES usuario(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Garante no máximo uma atribuição ATIVA por par (projeto, avaliador); reatribuição cancela a antiga antes de criar outra
CREATE UNIQUE INDEX uk_atribuicao_ativa ON atribuicao (projeto_id, avaliador_id) WHERE status = 'ATIVA';

CREATE TABLE avaliacao (
    id BIGSERIAL PRIMARY KEY,
    atribuicao_id BIGINT NOT NULL UNIQUE REFERENCES atribuicao(id),
    status VARCHAR(20) NOT NULL,
    nota_final NUMERIC(5,2),
    data_envio TIMESTAMPTZ,
    reaberta_em TIMESTAMPTZ,
    reaberta_por BIGINT REFERENCES usuario(id),
    motivo_reabertura TEXT
);

CREATE TABLE avaliacao_nota (
    id BIGSERIAL PRIMARY KEY,
    avaliacao_id BIGINT NOT NULL REFERENCES avaliacao(id),
    criterio_id BIGINT NOT NULL REFERENCES criterio(id),
    nota NUMERIC(5,2) NOT NULL,
    CONSTRAINT uk_avaliacao_criterio UNIQUE (avaliacao_id, criterio_id)
);

CREATE TABLE importacao_item (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL REFERENCES importacao_lote(id),
    referencia_externa VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    mensagem_erro TEXT,
    entidade_resultante_id BIGINT
);

CREATE TABLE log_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuario(id),
    acao VARCHAR(100) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    entidade_id BIGINT,
    detalhes TEXT,
    data_hora TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_atribuicao_avaliador ON atribuicao (avaliador_id);
CREATE INDEX idx_atribuicao_projeto ON atribuicao (projeto_id);
CREATE INDEX idx_projeto_edicao ON projeto (edicao_id);
CREATE INDEX idx_edicao_avaliador_edicao ON edicao_avaliador (edicao_id);
CREATE INDEX idx_importacao_item_lote ON importacao_item (lote_id);
CREATE INDEX idx_log_auditoria_usuario ON log_auditoria (usuario_id);
