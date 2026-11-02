-- Solicitações públicas não possuem usuário autenticado no momento da criação

ALTER TABLE solicitacao ALTER COLUMN criado_por_id DROP NOT NULL;
