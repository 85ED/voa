-- V8: auditoria append-only (eventos de status/pagamento/emissão)

CREATE TABLE auditoria_evento (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    actor_id UUID REFERENCES usuario (id),
    acao VARCHAR(64) NOT NULL,
    entidade_tipo VARCHAR(64) NOT NULL,
    entidade_id UUID NOT NULL,
    payload_json TEXT,
    ip VARCHAR(64),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_org_criado ON auditoria_evento (organizacao_id, criado_em DESC);
CREATE INDEX idx_auditoria_entidade ON auditoria_evento (entidade_tipo, entidade_id);
