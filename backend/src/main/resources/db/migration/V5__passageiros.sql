-- V5: passageiros (entidade separada de cliente) ligados a operações

CREATE TABLE passageiro (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    cliente_id UUID REFERENCES cliente (id),
    nome VARCHAR(255) NOT NULL,
    documento VARCHAR(32),
    data_nascimento DATE,
    email VARCHAR(320),
    telefone VARCHAR(32),
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_passageiro_org ON passageiro (organizacao_id);
CREATE INDEX idx_passageiro_cliente ON passageiro (cliente_id) WHERE cliente_id IS NOT NULL;

CREATE TABLE solicitacao_passageiro (
    solicitacao_id UUID NOT NULL REFERENCES solicitacao (id) ON DELETE CASCADE,
    passageiro_id UUID NOT NULL REFERENCES passageiro (id) ON DELETE CASCADE,
    PRIMARY KEY (solicitacao_id, passageiro_id)
);

CREATE TABLE venda_passageiro (
    venda_id UUID NOT NULL REFERENCES venda (id) ON DELETE CASCADE,
    passageiro_id UUID NOT NULL REFERENCES passageiro (id) ON DELETE CASCADE,
    PRIMARY KEY (venda_id, passageiro_id)
);
