-- V4: vendas, itens snapshot e idempotência

CREATE TABLE venda (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    cotacao_id UUID UNIQUE REFERENCES cotacao (id),
    solicitacao_id UUID REFERENCES solicitacao (id),
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    codigo VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ABERTA',
    total NUMERIC(19, 4) NOT NULL,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    vendedor_id UUID NOT NULL REFERENCES usuario (id),
    manual BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_cancelamento TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fechada_em TIMESTAMPTZ,
    UNIQUE (organizacao_id, codigo)
);

CREATE INDEX idx_venda_org_status ON venda (organizacao_id, status, criado_em DESC);

CREATE TABLE venda_item (
    id UUID PRIMARY KEY,
    venda_id UUID NOT NULL REFERENCES venda (id) ON DELETE CASCADE,
    tipo VARCHAR(32) NOT NULL,
    descricao VARCHAR(512) NOT NULL,
    quantidade NUMERIC(12, 4) NOT NULL,
    valor_unitario NUMERIC(19, 4) NOT NULL,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    ordem INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_venda_item_venda ON venda_item (venda_id);

CREATE TABLE venda_idempotencia (
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    idempotency_key VARCHAR(128) NOT NULL,
    venda_id UUID NOT NULL REFERENCES venda (id),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (organizacao_id, idempotency_key)
);
