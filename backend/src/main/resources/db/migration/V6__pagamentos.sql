-- V6: pagamentos, parcelas e trilha por pagamento

CREATE TABLE pagamento (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    venda_id UUID NOT NULL REFERENCES venda (id),
    forma VARCHAR(32) NOT NULL,
    valor_total NUMERIC(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDENTE',
    observacoes TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estornado_em TIMESTAMPTZ,
    estornado_por_id UUID REFERENCES usuario (id),
    CONSTRAINT chk_pagamento_status CHECK (
        status IN ('PENDENTE', 'PARCIAL', 'QUITADO', 'ESTORNADO')
    )
);

CREATE INDEX idx_pagamento_venda ON pagamento (venda_id);
CREATE INDEX idx_pagamento_org_status ON pagamento (organizacao_id, status);

CREATE TABLE parcela (
    id UUID PRIMARY KEY,
    pagamento_id UUID NOT NULL REFERENCES pagamento (id) ON DELETE CASCADE,
    numero INT NOT NULL,
    vencimento DATE NOT NULL,
    valor NUMERIC(19, 4) NOT NULL,
    comprovante_ref VARCHAR(512),
    pago_em TIMESTAMPTZ,
    confirmado_por_id UUID REFERENCES usuario (id),
    confirmado_em TIMESTAMPTZ,
    UNIQUE (pagamento_id, numero)
);

CREATE INDEX idx_parcela_pagamento ON parcela (pagamento_id);

CREATE TABLE pagamento_evento (
    id UUID PRIMARY KEY,
    pagamento_id UUID NOT NULL REFERENCES pagamento (id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES usuario (id),
    tipo VARCHAR(64) NOT NULL,
    payload_json TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pagamento_evento_pagamento ON pagamento_evento (pagamento_id, criado_em DESC);
