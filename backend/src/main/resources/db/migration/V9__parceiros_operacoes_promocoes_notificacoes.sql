-- Parceiros, operações (vínculo venda), linhas de comissão manual, promoções e notificações internas

CREATE TABLE parceiro (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    telefone VARCHAR(32),
    documento VARCHAR(32),
    percentual_referencia NUMERIC(7, 4),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_parceiro_org ON parceiro (organizacao_id);
CREATE INDEX idx_parceiro_org_nome ON parceiro (organizacao_id, nome);

CREATE TABLE operacao (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    venda_id UUID NOT NULL REFERENCES venda (id),
    parceiro_id UUID REFERENCES parceiro (id),
    codigo VARCHAR(64) NOT NULL,
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_operacao_venda UNIQUE (venda_id)
);

CREATE INDEX idx_operacao_org ON operacao (organizacao_id, criado_em DESC);
CREATE INDEX idx_operacao_parceiro ON operacao (parceiro_id);

CREATE TABLE operacao_comissao_linha (
    id UUID PRIMARY KEY,
    operacao_id UUID NOT NULL REFERENCES operacao (id) ON DELETE CASCADE,
    descricao VARCHAR(512) NOT NULL,
    valor NUMERIC(19, 4) NOT NULL,
    manual BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_operacao_comissao_operacao ON operacao_comissao_linha (operacao_id);

CREATE TABLE comissao_regra (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    percentual NUMERIC(7, 4),
    base_calculo VARCHAR(64),
    ativo BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE promocao (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(2048),
    slug VARCHAR(128) NOT NULL,
    destaque BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    valido_de DATE,
    valido_ate DATE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_promocao_org_slug UNIQUE (organizacao_id, slug)
);

CREATE INDEX idx_promocao_org_ativo ON promocao (organizacao_id, ativo, valido_ate);

CREATE TABLE notificacao_interna (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    usuario_id UUID REFERENCES usuario (id),
    titulo VARCHAR(255) NOT NULL,
    corpo TEXT,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    entidade_tipo VARCHAR(64),
    entidade_id UUID,
    criado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notificacao_usuario ON notificacao_interna (organizacao_id, usuario_id, lida, criado_em DESC);
