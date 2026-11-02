-- V7: bilhetes / emissões ligados à venda

CREATE TABLE bilhete (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    venda_id UUID NOT NULL REFERENCES venda (id),
    tipo VARCHAR(32) NOT NULL DEFAULT 'AEREO',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDENTE',
    localizador VARCHAR(64),
    emissor VARCHAR(128),
    arquivo_path VARCHAR(1024),
    arquivo_nome VARCHAR(255),
    arquivo_mime VARCHAR(128),
    agendado_para TIMESTAMPTZ,
    emitido_em TIMESTAMPTZ,
    enviado_cliente_em TIMESTAMPTZ,
    metadados_json TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_bilhete_status CHECK (
        status IN ('PENDENTE', 'AGENDADA', 'EMITIDA', 'ENVIADA_CLIENTE', 'CANCELADA')
    )
);

CREATE INDEX idx_bilhete_venda ON bilhete (venda_id);
CREATE INDEX idx_bilhete_org_status ON bilhete (organizacao_id, status);
