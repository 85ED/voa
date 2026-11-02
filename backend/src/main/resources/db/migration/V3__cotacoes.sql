-- V3: cotações versionadas, itens, alternativas de voo e histórico

CREATE TABLE cotacao (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    solicitacao_id UUID NOT NULL REFERENCES solicitacao (id),
    versao INT NOT NULL,
    validade DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'RASCUNHO',
    total_bruto NUMERIC(19, 4) NOT NULL DEFAULT 0,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    observacoes TEXT,
    criado_por_id UUID NOT NULL REFERENCES usuario (id),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (solicitacao_id, versao)
);

CREATE INDEX idx_cotacao_solicitacao ON cotacao (solicitacao_id, versao DESC);
CREATE INDEX idx_cotacao_org_status ON cotacao (organizacao_id, status);

CREATE TABLE cotacao_item (
    id UUID PRIMARY KEY,
    cotacao_id UUID NOT NULL REFERENCES cotacao (id) ON DELETE CASCADE,
    tipo VARCHAR(32) NOT NULL,
    descricao VARCHAR(512) NOT NULL,
    quantidade NUMERIC(12, 4) NOT NULL DEFAULT 1,
    valor_unitario NUMERIC(19, 4) NOT NULL,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    ordem INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cotacao_item_cotacao ON cotacao_item (cotacao_id);

CREATE TABLE cotacao_alternativa (
    id UUID PRIMARY KEY,
    cotacao_id UUID NOT NULL REFERENCES cotacao (id) ON DELETE CASCADE,
    rotulo VARCHAR(64) NOT NULL,
    resumo VARCHAR(512),
    origem VARCHAR(8),
    destino VARCHAR(8),
    valor_total NUMERIC(19, 4) NOT NULL,
    moeda VARCHAR(3) NOT NULL DEFAULT 'BRL',
    detalhes_json JSONB,
    ordem INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_cotacao_alternativa_cotacao ON cotacao_alternativa (cotacao_id, ordem);

CREATE TABLE cotacao_evento (
    id UUID PRIMARY KEY,
    cotacao_id UUID NOT NULL REFERENCES cotacao (id) ON DELETE CASCADE,
    de_status VARCHAR(32),
    para_status VARCHAR(32) NOT NULL,
    actor_id UUID NOT NULL REFERENCES usuario (id),
    observacao TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cotacao_evento_cotacao ON cotacao_evento (cotacao_id, criado_em DESC);
