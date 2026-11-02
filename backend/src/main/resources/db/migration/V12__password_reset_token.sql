-- Tokens de redefinição de senha (uso único, hash SHA-256)

CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    usado_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_usuario ON password_reset_token (usuario_id);
CREATE UNIQUE INDEX uq_password_reset_hash ON password_reset_token (token_hash);
