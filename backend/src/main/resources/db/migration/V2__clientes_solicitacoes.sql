-- V2: clientes e solicitações de viagem

CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    telefone VARCHAR(32),
    documento VARCHAR(32),
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cliente_org ON cliente (organizacao_id);
CREATE INDEX idx_cliente_org_documento ON cliente (organizacao_id, documento) WHERE documento IS NOT NULL;

CREATE TABLE solicitacao (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    titulo VARCHAR(255) NOT NULL,
    destino VARCHAR(255),
    data_ida DATE,
    data_volta DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'RASCUNHO',
    responsavel_id UUID REFERENCES usuario (id),
    criado_por_id UUID NOT NULL REFERENCES usuario (id),
    observacoes TEXT,
    motivo_cancelamento TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_solicitacao_org_status ON solicitacao (organizacao_id, status, criado_em DESC);
CREATE INDEX idx_solicitacao_cliente ON solicitacao (cliente_id);
