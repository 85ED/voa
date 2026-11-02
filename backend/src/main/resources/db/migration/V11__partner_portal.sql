-- Portal parceiro: slug, usuário PARTNER, canal e vínculo em solicitação/venda

ALTER TABLE parceiro ADD COLUMN slug VARCHAR(128);
ALTER TABLE parceiro ADD COLUMN nome_comercial VARCHAR(255);
ALTER TABLE parceiro ADD COLUMN subtitulo VARCHAR(512);

UPDATE parceiro
SET slug = lower(regexp_replace(regexp_replace(trim(nome), '[^a-zA-Z0-9]+', '-', 'g'), '(^-|-$)', '', 'g'))
WHERE slug IS NULL;

UPDATE parceiro SET slug = replace(id::text, '-', '') WHERE slug IS NULL OR slug = '';

ALTER TABLE parceiro ALTER COLUMN slug SET NOT NULL;
CREATE UNIQUE INDEX uq_parceiro_org_slug ON parceiro (organizacao_id, slug);

CREATE TABLE parceiro_slug_historico (
    id UUID PRIMARY KEY,
    parceiro_id UUID NOT NULL REFERENCES parceiro (id) ON DELETE CASCADE,
    slug VARCHAR(128) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_parceiro_slug_hist ON parceiro_slug_historico (parceiro_id, criado_em DESC);

ALTER TABLE usuario ADD COLUMN parceiro_id UUID REFERENCES parceiro (id);
CREATE INDEX idx_usuario_parceiro ON usuario (parceiro_id);

ALTER TABLE solicitacao ADD COLUMN parceiro_id UUID REFERENCES parceiro (id);
ALTER TABLE solicitacao ADD COLUMN canal VARCHAR(32) NOT NULL DEFAULT 'INTERNO';
ALTER TABLE solicitacao ADD COLUMN parceiro_slug_snapshot VARCHAR(128);
ALTER TABLE solicitacao ADD COLUMN origem VARCHAR(255);
ALTER TABLE solicitacao ADD COLUMN metadados_json TEXT;

CREATE INDEX idx_solicitacao_parceiro ON solicitacao (organizacao_id, parceiro_id, criado_em DESC);

ALTER TABLE venda ADD COLUMN parceiro_id UUID REFERENCES parceiro (id);
CREATE INDEX idx_venda_parceiro ON venda (organizacao_id, parceiro_id);

ALTER TABLE usuario_papel DROP CONSTRAINT IF EXISTS chk_usuario_papel;
ALTER TABLE usuario_papel ADD CONSTRAINT chk_usuario_papel CHECK (
    papel IN ('ADMIN', 'COMERCIAL', 'OPERACOES', 'FINANCEIRO', 'AUDITOR', 'PARTNER')
);
