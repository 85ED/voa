# Escopo do projeto VOA

VOA é a plataforma operacional da **JL Turismo** para solicitações de viagem, cotações, vendas, pagamentos, bilhetes e auditoria.

## Produto

| Item | Decisão |
|------|---------|
| Cliente | JL Turismo (single tenant no MVP) |
| Stack | Spring Boot 3, PostgreSQL, Flyway, React/Vite/TS |
| Deploy | Railway (projeto separado do Guia de Corrida) |
| Idioma | Português (BR) |

## Escopo desta fundação (Etapa 2 + auth + solicitações)

- Monorepo `backend/` + `frontend/` + `docker-compose.yml`
- Flyway **V1** (organização, usuário, papéis, refresh token) e **V2** (clientes, solicitações)
- Autenticação JWT (15 min) + refresh rotacionado em cookies HttpOnly (ADR-002)
- API REST `/api/v1/auth/*`, `/api/v1/clientes`, `/api/v1/solicitacoes`
- Testes de integração com **Testcontainers** (Postgres)
- SPA com login e shell autenticado

## Fora de escopo

- GDS, gateway de pagamento, emissão automática de bilhetes
- Cálculo automático de comissão 4% (`COMISSAO_AUTO_ENABLED=false`, ADR-003)
- Alterações no repositório Guia de Corrida

## Documentação

Ver `docs/architecture.md`, `docs/api.md`, `docs/security.md` e ADRs em `docs/decisions/`.
