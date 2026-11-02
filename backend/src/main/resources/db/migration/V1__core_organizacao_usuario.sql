-- V1: organização, usuários, papéis e refresh tokens (ADR-002)

CREATE TABLE organizacao (
    id UUID PRIMARY KEY,
    razao_social VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14),
    timezone VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo',
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE usuario (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL REFERENCES organizacao (id),
    email VARCHAR(320) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_org_email UNIQUE (organizacao_id, email)
);

CREATE INDEX idx_usuario_org ON usuario (organizacao_id);

CREATE TABLE usuario_papel (
    usuario_id UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    papel VARCHAR(32) NOT NULL,
    PRIMARY KEY (usuario_id, papel),
    CONSTRAINT chk_usuario_papel CHECK (papel IN ('ADMIN', 'COMERCIAL', 'OPERACOES', 'FINANCEIRO', 'AUDITOR'))
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
CREATE UNIQUE INDEX uq_refresh_token_hash ON refresh_token (token_hash);
